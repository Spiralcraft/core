//
// Copyright (c) 1998,2007 Michael Toth
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
import java.util.HashMap;
import java.util.LinkedHashMap;

import spiralcraft.common.LifecycleException;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Key;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Sequence;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.UniqueKeyViolationException;
import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.access.Schema;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.access.Updater;
import spiralcraft.data.access.Table;

import spiralcraft.data.core.SequenceField;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;
import spiralcraft.data.sax.DataWriter;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.data.session.BufferType;
import spiralcraft.data.spi.AbstractStore;
import spiralcraft.data.spi.BaseExtentQueryable;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.SimpleChannel;


/**
 * Provides access to XML data 
 *
 * @author mike
 *
 */
public class XmlStore
  extends AbstractStore
{

  
  private LinkedHashMap<Type<?>,Queryable<Tuple>> queryables
    =new LinkedHashMap<Type<?>,Queryable<Tuple>>();
    
  private ArrayList<XmlQueryable> xmlQueryables
    =new ArrayList<XmlQueryable>();
  
  
  private URI baseResourceURI;
  
//  private Focus<XmlStore> focus;
  
  private Type<?> sequenceType;
  private HashMap<URI,XmlSequence> sequences;
  private EquiJoin sequenceQuery;
  private XmlQueryable sequenceQueryable
    =new XmlQueryable();

  private Schema schema;
  
  public XmlStore()
  {
//      focus=new SimpleFocus<XmlStore>
//        (new SimpleChannel<XmlStore>(this,true)
//        );
    try
    {
      sequenceType=Type.resolve("class:/spiralcraft/data/spi/Sequence");
      
      sequenceQueryable.setResultType(sequenceType);
      sequenceQueryable.setResourceURI(URI.create("Sequence.xml"));
      sequenceQueryable.setAutoCreate(true);
      
      sequenceQuery=new EquiJoin();
      sequenceQuery.setSource(new Scan(sequenceType));
      sequenceQuery.setAssignments(".uri=..");
//      sequenceQuery.setDebug(true);
      sequenceQuery.resolve();
      
    }
    catch (DataException x)
    { throw new RuntimeDataException("Error resolving Sequence type",x);
    }
  }
  
  public void setBaseResourceURI(URI uri)
  { baseResourceURI=uri;
  }
  
  public void setSchema(Schema schema)
  { this.schema=schema;
  }
  
//  public XmlQueryable[] getQueryables()
//  { 
//    XmlQueryable[] list=new XmlQueryable[queryables.size()];
//    queryables.values().toArray(list);
//    return list;
//    
//  }
  
  
  private void addQueryable(XmlQueryable queryable)
  {
    xmlQueryables.add(queryable);
    
    Type<?> subtype=queryable.getResultType();
    queryables.put(subtype,queryable);
    
    addBaseTypes(queryable);    
  }
    
  public void setQueryables(XmlQueryable[] list)
  { 
    
    for (XmlQueryable queryable:list)
    { addQueryable(queryable);
    }
  }
  
  private void addSequences(Type<?> subtype)
  {
      if (subtype.getScheme()!=null)
      {
        if (sequences==null)
        { sequences=new HashMap<URI,XmlSequence>();
        }
        for (Field<?> field : subtype.getScheme().fieldIterable())
        { 
          if (field instanceof SequenceField)
          { 
            sequences.put
            (field.getURI()
            ,new XmlSequence(field.getURI())
            );
          }
        }
      }
    
  }
 
  /**
   * <p>Make sure any base-type "union proxies" are set up, to translate a 
   *   Query for the base-type into a union of subtypes.
   * </p>
   * 
   * @param queryable
   */
  @SuppressWarnings("unchecked")
  private void addBaseTypes(XmlQueryable queryable)
  {
      Type<?> subtype=queryable.getResultType();
      Type<?> type=subtype.getBaseType();
      while (type!=null)
      { 
        // Set up a queryable for each of the XmlQueryable's base types
        
        Queryable<?> candidateQueryable=queryables.get(type);
        BaseExtentQueryable baseQueryable;
        
        if (candidateQueryable==null)
        { 
          baseQueryable=new BaseExtentQueryable(type);
          queryables.put(type, baseQueryable);
          baseQueryable.addExtent(subtype,queryable);
        }
        else if (!(candidateQueryable instanceof BaseExtentQueryable))
        {
          // The base extent queryable is already "concrete"
          // This is ambiguous, though. The base extent queryable only
          //   contains the non-subtyped concrete instances of the
          //   base type.
          
          baseQueryable=new BaseExtentQueryable(type);
          queryables.put(type, baseQueryable);
          baseQueryable.addExtent(type,candidateQueryable);
          baseQueryable.addExtent(subtype,queryable);
        }
        else
        {
          ((BaseExtentQueryable) candidateQueryable)
            .addExtent(subtype, queryable);
        }
        type=type.getBaseType();
        
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
    { queryable=queryables.get(type.getArchetype());
    }
    else
    { queryable=queryables.get(type);
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
  protected Queryable<Tuple> getQueryable(Type<?> type)
  { return queryables.get(type);
  }
  

  
  


  @Override
  public Type<?>[] getTypes()
  {
    Type<?>[] types=new Type[queryables.size()];
    int i=0;
    for (Queryable<Tuple> queryable: queryables.values())
    { types[i++]=queryable.getTypes()[0];
    }
    return types;
  }



  @Override
  public void start()
    throws LifecycleException
  {
    
    if (schema!=null)
    {
      for (Table table: schema.getTables())
      {
        XmlQueryable queryable=new XmlQueryable();
        queryable.setResultType(table.getType());
        queryable.setResourceURI(URI.create(table.getStoreName()+".data.xml"));
        queryable.setAutoCreate(true);
        addQueryable(queryable);
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

    for (Queryable<?> queryable:queryables.values())
    { addSequences(queryable.getTypes()[0]);
    }
    
    for (XmlSequence sequence : sequences.values())
    { 
      try
      { sequence.init();
      }
      catch (DataException x)
      { throw new LifecycleException("Error initializing sequence "+sequence,x);
      }
      
    }
    
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    // TODO Auto-generated method stub
    
  }


  public Sequence getSequence(URI uri)
  {
    Sequence sequence=sequences.get(uri);
    return sequence;
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
            if (cursor.dataNext())
            { 
              throw new UniqueKeyViolationException
                (tuple,uniqueKeys.get(i));
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
            if (cursor.dataNext())
            { 
              if (!cursor.dataGetTuple().equals(tuple.getOriginal()))
              {
                if (debug)
                { 
                  log.fine("\r\n  existing="+cursor.dataGetTuple()
                    +"\r\n  new="+tuple.getOriginal()
                    +"\r\n updated="+tuple
                    );
                }
                throw new UniqueKeyViolationException
                  (tuple,uniqueKeys.get(i));
              }
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
      { log.fine("Finalizing "+queryable.getResultType());
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
                  if (!cursor.dataNext())
                  {
                    // Old one has been deleted
                    log.warning("Adding back lost original on update"+t); 
                    queryable.add(t);
                  }
                  else
                  { 
                    EditableTuple newOriginal=(EditableTuple) cursor.dataGetTuple();
                    if (newOriginal!=original)
                    { 
                      if (debug)
                      { log.fine("Updating original to new version "+newOriginal);
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

                  if (cursor.dataNext())
                  {
                    log.warning
                    ("Cardinality violation: duplicate "
                      +cursor.dataGetTuple()
                    );
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

    public void init()
      throws DataException
    {
      boundQuery
        =sequenceQueryable.query(sequenceQuery,uriFocus);
    }
    
    public void allocate()
      throws DataException
    {
      synchronized(sequenceQueryable)
      {
        SerialCursor<Tuple> result=boundQuery.execute();
        if (!result.dataNext())
        {
          EditableTuple row=new EditableArrayTuple(sequenceType.getScheme());
          sequenceType.<URI>getField("uri").setValue(row,uri);
          sequenceType.getField("nextValue").setValue(row,200);
          sequenceType.getField("increment").setValue(row,100);
          next=100;
          stop=200;
          increment=100;
          sequenceQueryable.add(row);
        }
        else
        {
          EditableTuple row=(EditableTuple) result.dataGetTuple();
          next=(Integer) sequenceType.getField("nextValue").getValue(row);
          increment=(Integer) sequenceType.getField("increment").getValue(row);
          
          stop=next+increment;
          sequenceType.getField("nextValue").setValue(row,next+increment);
          
        }
        if (result.dataNext())
        {
          throw new DataException
            ("Cardinality violation in Sequence store- non unique URI "+uri); 
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
