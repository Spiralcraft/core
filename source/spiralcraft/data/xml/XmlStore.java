//
// Copyright (c) 1998,2009 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data.xml;

import java.io.IOException;

import java.net.URI;

import java.util.ArrayList;

import spiralcraft.common.LifecycleException;
import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Key;
import spiralcraft.data.Sequence;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.UniqueKeyViolationException;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.access.Updater;
import spiralcraft.data.access.Entity;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.sax.DataWriter;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.data.session.BufferType;
import spiralcraft.data.spi.AbstractStore;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.Level;


/**
 * Provides access to XML data 
 *
 * @author mike
 *
 */
public class XmlStore
  extends AbstractStore
{

  private ArrayList<XmlQueryable> xmlQueryables
    =new ArrayList<XmlQueryable>();
  
  private URI baseResourceURI;
  
  private XmlQueryable sequenceQueryable
    =new XmlQueryable();

  
  public XmlStore()
    throws DataException
  {
    sequenceQueryable.setResultType(sequenceType);
    sequenceQueryable.setResourceURI(URI.create("Sequence.xml"));
    sequenceQueryable.setAutoCreate(true);

  }
  
  public void setBaseResourceURI(URI uri)
  { baseResourceURI=uri;
  }

  public void setQueryables(XmlQueryable[] list)
  { 
    
    for (XmlQueryable queryable:list)
    { addQueryable(queryable);
    }
  }

  
  @SuppressWarnings("unchecked")
  @Override
  public DataConsumer<DeltaTuple> getUpdater(
    Type<?> type,Focus<?> focus)
    throws DataException
  {
    Queryable queryable;
    
    if (type instanceof BufferType)
    { queryable=getQueryable(type.getArchetype());
    }
    else
    { queryable=getQueryable(type);
    }
    
    if (queryable==null)
    { return null;
    }
    if (!(queryable instanceof XmlQueryable))
    { throw new DataException("Cannot update an abstract type");
    }
      
    return new XmlUpdater(focus,(XmlQueryable) queryable);

  }

  @Override
  public void start()
    throws LifecycleException
  {
    
    if (schema!=null)
    {

      
      for (Entity entity: schema.getEntities())
      {
        XmlQueryable queryable=new XmlQueryable();
        queryable.setResultType(entity.getType());
        queryable.setResourceURI(URI.create(entity.getName()+".data.xml"));
        queryable.setAutoCreate(true);
        addQueryable(queryable);
        
        if (debugLevel.canLog(Level.DEBUG))
        { log.debug("Added XmlQueryable from schema");
        }        
      }
    }
    
    for (XmlQueryable queryable:xmlQueryables)
    { 
      try
      { 
        queryable.setResourceContextURI(baseResourceURI);
        queryable.getAll(queryable.getResultType());
      }
      catch (DataException x)
      {
        // TODO Auto-generated catch block
        x.printStackTrace();
      }
    }
    
    try
    { 
      sequenceQueryable.setResourceContextURI(baseResourceURI);
      sequenceQueryable.getAll(sequenceQueryable.getResultType());
    }
    catch (DataException x)
    { x.printStackTrace();
    }

    super.start();
  }

  @Override
  public void stop()
    throws LifecycleException
  { super.stop();
  }
  
  
  @Override
  protected Sequence createSequence(Field<?> field)
  { return new XmlSequence(field.getURI());
  }

  
 
  private void addQueryable(XmlQueryable queryable)
  {
    xmlQueryables.add(queryable);
    Type<?> subtype=queryable.getResultType();
    addPrimaryQueryable(subtype,queryable);
  }
    
  




  
  class XmlUpdater
    extends Updater<DeltaTuple>
  {
    private XmlQueryable queryable;
    private ArrayList<Tuple> addList=new ArrayList<Tuple>();
    private ArrayList<Tuple> updateList=new ArrayList<Tuple>();
    private ArrayList<Tuple> deleteList=new ArrayList<Tuple>();

    private BoundQuery<?,Tuple> originalQuery;
    
    private ArrayList<BoundQuery<?,Tuple>> uniqueQueries
      =new ArrayList<BoundQuery<?,Tuple>>();
    private ArrayList<Key<?>> uniqueKeys
      =new ArrayList<Key<?>>();
    
    private DataWriter debugWriter=new DataWriter();
    
    
    public XmlUpdater(Focus<?> context,XmlQueryable queryable)
    { 
      super(context);
      this.queryable=queryable;
    }
    
    @Override
    public void dataInitialize(FieldSet fieldSet)
      throws DataException
    { 
      super.dataInitialize(fieldSet);
      Type<?> type=queryable.getResultType();
      Key<?> primaryKey=type.getPrimaryKey();
      if (primaryKey!=null)
      { originalQuery=queryable.query(primaryKey.getQuery(), localFocus);
      }
      
      for (Key<?> key: type.getScheme().keyIterable())
      {
        if (key.isUnique() || key.isPrimary())
        { 
          // Create queries for unique keys and associate the Key 
          //   with the query via a parallel list for error reporting.
          uniqueQueries.add(queryable.query(key.getQuery(),localFocus));
          uniqueKeys.add(key);
        }
      }
      
      // Make sure queryable has had a chance to init.
      queryable.getAggregate();
      
    }
    
    @Override
    public void dataAvailable(DeltaTuple tuple)
      throws DataException
    {
      super.dataAvailable(tuple);
      if (tuple.getOriginal()==null && !tuple.isDelete())
      { 
        // New case
        
        // Check unique keys
        localChannel.push(tuple);
        try
        {
          int i=0;
          for (BoundQuery<?,Tuple> query: uniqueQueries)
          {
            SerialCursor<Tuple> cursor=query.execute();
            try
            {
              if (cursor.next())
              { 
                throw new UniqueKeyViolationException
                  (tuple,uniqueKeys.get(i));
              }
            }
            finally
            { cursor.close();
            }
            i++;
            
          }
        }
        finally
        { localChannel.pop();
        }
        
        // Copy
        EditableArrayTuple newTuple;
        if (tuple instanceof BufferTuple)
        { 
          newTuple
            =new EditableArrayTuple(tuple.getFieldSet().getType()
              .getArchetype().getScheme()
              );
          ((BufferTuple) tuple).updateTo(newTuple);
          ((BufferTuple) tuple).updateOriginal(newTuple);
          
        } 
        else
        { newTuple=new EditableArrayTuple(tuple);
        }
        
        addList.add(newTuple);
        

        if (debug)
        {
          debugWriter.writeToOutputStream(System.out, tuple);
          System.out.flush();
        }
      }
      else if (!tuple.isDelete())
      { 
        // Check unique keys
        localChannel.push(tuple);
        try
        {
          int i=0;
          for (BoundQuery<?,Tuple> query: uniqueQueries)
          {
            SerialCursor<Tuple> cursor=query.execute();
            try
            {
              if (cursor.next())
              { 
                if (!cursor.getTuple().equals(tuple.getOriginal()))
                {
                  // XXX We need to check if tuple is a later version of
                  //  original, and then check for an update conflict
                  if (debug)
                  { 
                    log.fine("\r\n  existing="+cursor.getTuple()
                      +"\r\n  new="+tuple.getOriginal()
                      +"\r\n updated="+tuple
                      );
                  }
                  throw new UniqueKeyViolationException
                    (tuple,uniqueKeys.get(i));
                }
              }
            }
            finally
            { cursor.close();
            }
            i++;
            
          }
        }
        finally
        { localChannel.pop();
        }
        // Update case
        updateList.add(tuple);
      }
      else
      { 
        // Delete case
        deleteList.add(tuple.getOriginal());
      }
    }
    
    @Override
    public void dataFinalize()
      throws DataException
    { 
      if (debug)
      { log.fine("Finalizing updater for "+queryable.getResultType());
      }
      synchronized (queryable)
      {
        queryable.freeze();
        try
        {
        
          for (Tuple t: deleteList)
          { queryable.remove(t);
          }

          for (Tuple t: addList)
          { queryable.add(t);
          }

          for (Tuple t: updateList)
          {
            if (t instanceof DeltaTuple)
            { 
              DeltaTuple dt=(DeltaTuple) t;

              // Must find the old copy
              EditableTuple original=(EditableTuple) dt.getOriginal();
              if (originalQuery!=null)
              {
                localChannel.push(dt);
                try
                {
                
                  // Find a more certain original
                  SerialCursor<Tuple> cursor=originalQuery.execute();
                  try
                  {
                    if (!cursor.next())
                    {
                      // Old one has been deleted
                      log.warning("Adding back lost original on update"+t); 
                      queryable.add(t);
                    }
                    else
                    { 
                      EditableTuple newOriginal
                        =(EditableTuple) cursor.getTuple();
                      if (newOriginal!=original)
                      { 
                        if (debug)
                        { 
                          log.fine
                            ("Updating original to new version "+newOriginal);
                        }
                        if (t instanceof BufferTuple)
                        { ((BufferTuple) t).updateOriginal(newOriginal);
                        }
                        original=newOriginal;

                      }
                      else
                      { 
                        if (debug)
                        { log.fine("Read back same data"+original);
                        }
                      }
                    }

                    if (cursor.next())
                    {
                      log.warning
                      ("Cardinality violation: duplicate "
                        +cursor.getTuple()
                      );
                    }
                  }
                  finally
                  { cursor.close();
                  }
                }
                finally
                { localChannel.pop();
                }
              }
              else
              { log.fine("No primary key defined for "+t.getType());
              }

              // Really should be working on a copy here, but that's what Journaling
              //   is for.
              dt.updateTo(original);
            }
          }


          try
          {
            queryable.flush(".new");

            // XXX Needs to be in transactional resource as LLR
            queryable.commit(".new");
            
            // Make -sure- we re-read to verify data and fail for transaction
            //   user
            queryable.refresh();
          }
          catch (IOException x)
          { throw new DataException("IOException persisting data",x);
          }
     
        }
        finally
        { queryable.unfreeze();
        }
      
      }
    }
  }
  
  class XmlSequence
    implements Sequence
  {

    private int increment;
    private volatile int next;
    private volatile int stop;
    private BoundQuery<?,Tuple> boundQuery;
    private Focus<URI> uriFocus;
    private URI uri;
    
    public XmlSequence (URI uri)
    { 
      this.uri=uri;
      uriFocus=new SimpleFocus<URI>(new SimpleChannel<URI>(uri,true));
    }

    public void start()
      throws LifecycleException
    {
      try
      {
        boundQuery
          =sequenceQueryable.query(sequenceQuery,uriFocus);
      }
      catch (DataException x)
      { 
        throw new LifecycleException
          ("Error binding sequence query for "+uri,x);
      }
    }
    
    public void stop()
    {
    }
    
    public void allocate()
      throws DataException
    {
      synchronized(sequenceQueryable)
      {
        SerialCursor<Tuple> result=boundQuery.execute();
        try
        {
          if (!result.next())
          {
            EditableTuple row=new EditableArrayTuple(sequenceType.getScheme());
            row.set("uri",uri);
            row.set("nextValue",200);
            row.set("increment",100);

            next=100;
            stop=200;
            increment=100;
            sequenceQueryable.add(row);
          }
          else
          {
            EditableTuple row=(EditableTuple) result.getTuple();
            next=(Integer) row.get("nextValue");
            increment=(Integer) row.get("increment");
          
            stop=next+increment;
            row.set("nextValue",next+increment);
          
          }
          if (result.next())
          {
            throw new DataException
              ("Cardinality violation in Sequence store- non unique URI "+uri); 
          }
        }
        finally
        { result.close();
        }
        
        try
        {
          sequenceQueryable.flush(".new");
          sequenceQueryable.commit(".new");
        }
        catch (IOException x)
        { throw new DataException
            ("Error writing data while allocating sequence",x); 
        }
        
      }
    }
    
    @Override
    public synchronized Integer next()
      throws DataException
    {
      if (next==stop)
      { allocate();
      }
      return next++;
    }
  }
}
