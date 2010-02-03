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

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Scan;
import spiralcraft.data.spi.AbstractAggregateQueryable;
import spiralcraft.data.spi.EditableKeyedListAggregate;
import spiralcraft.data.spi.KeyedListAggregate;

import spiralcraft.data.sax.DataReader;
import spiralcraft.data.sax.DataWriter;
import spiralcraft.data.transaction.Branch;
import spiralcraft.data.transaction.ResourceManager;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.Transaction.Nesting;
import spiralcraft.data.transaction.Transaction.State;

import spiralcraft.data.Aggregate;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;


import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.watcher.ResourceWatcher;
import spiralcraft.vfs.watcher.WatcherHandler;


import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.IOException;

import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>Provides basic query functionality for an XML document which contains 
 *   an Aggregate (a set of Tuples of a common Type)
 * </p>
 * 
 * <p>The document is polled for updates
 * </p>
 * 
 * @author mike
 */
public class XmlQueryable
  extends AbstractAggregateQueryable<Tuple>
{
  private static final ClassLog log
    =ClassLog.getInstance(XmlQueryable.class);
  
  
  private SimpleDateFormat dateFormat
    =new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
  
  private Resource resource;
  private URI resourceURI;
  private URI resourceContextURI;
  
  private ResourceWatcher watcher;
  
  private Type<?> type;
  
  private KeyedListAggregate<Tuple> aggregate;
  private Exception exception;
  
  private boolean autoCreate;
  
  private boolean debug;
  private boolean frozen;
  private long lastTransactionId;
  
  private XmlResourceManager resourceManager
    =new XmlResourceManager();
  
  private volatile Transaction transaction;
  
  public long getLastTransactionId()
  { return lastTransactionId;
  }
  
  private WatcherHandler handler
    =new WatcherHandler()
    {
      @SuppressWarnings("unchecked") // Downcast
      public int handleUpdate(Resource resource)
      {
        DataReader reader=new DataReader();
        try
        {
          exception=null;
          if (debug)
          { log.fine("Resource "+resource.getURI()+" changed");
          }
          Aggregate<Tuple> origData
            =(Aggregate<Tuple>) reader.readFromResource
              (resource, type);
          
          EditableKeyedListAggregate data
            =new EditableKeyedListAggregate(origData);
          setAggregate(data);
        }
        catch (Exception x)
        { 
          exception=x;
          x.printStackTrace();
          return -1000;
        }
        return 0;
      }
    };
  
  public XmlQueryable() 
  { super();
  }
  
  public XmlQueryable(Type<?> type,URI resourceURI)
  {
    this.type=type;
    setResourceURI(resourceURI);
  }
  
  @Override
  public BoundQuery<?,Tuple> query(Query q, Focus<?> context)
    throws DataException
  {   
    BoundQuery<?,Tuple> ret;
    if ( (q instanceof EquiJoin)
        && (q.getSources().get(0) instanceof Scan)
        && q.getType().isAssignableFrom(getResultType())
        )
    { 
      
      EquiJoin ej=(EquiJoin) q;
      
      ret=new BoundIndexScan(ej,context);
      ret.resolve();
      return ret;
      
    }
    else 
    { return super.query(q, context);
    }
  }
  
  protected void checkInit()
    throws DataException
  {
    if (type==null)
    { throw new DataException("No Type configured");
    }

    
    if (resource==null)
    { 
      if (resourceURI==null)
      { throw new DataException("No resourceURI specified");
      }
      
      try
      { 
        URI qualifiedURI
          =resourceURI.isAbsolute()
          ?resourceURI
          :resourceContextURI!=null
            ?resourceContextURI.resolve(resourceURI)
            :resourceURI
          ;
            
        Resource resource=Resolver.getInstance().resolve(qualifiedURI);
        setResource(resource);
        try
        {
          if (!resource.exists())
          { 
            if (autoCreate)
            {
              aggregate
                =new EditableKeyedListAggregate<Tuple>(type);
              flush(null);
            }
            else
            { throw new DataException(qualifiedURI+" does not exist");
            }
          }
        }
        catch (IOException x)
        { throw new DataException(qualifiedURI+" could not be read",x);
        }
      }
      catch (UnresolvableURIException x)
      { 
        throw new DataException
          ("Error resolving "+resourceURI
          +(resourceContextURI!=null
           ?" in context "+resourceContextURI
           :""
           )
          ,x
          );
      }
      
      
    }
          
    if (watcher==null)
    { watcher=new ResourceWatcher(resource,1000,handler);    
    }
  }
  
  @Override
  protected Aggregate<Tuple> getAggregate()
    throws DataException
  { 
    checkInit();

    // log.fine("Checking resource");
    if (!frozen)
    {
      try
      { watcher.check();
      }
      catch (IOException x)
      { exception=x;
      }
      
      if (exception!=null)
      { throw new DataException("Error loading data from "+resourceURI,exception);
      }
    }
    return aggregate;
  }
  
  @Override
  // This is the Type of the Queryable, not the data container
  protected Type<?> getResultType()
  { 
    if (type!=null)
    { return type.getContentType();
    }
    else
    { return null;
    }
  }
  
  public void setResultType(Type<?> type)
  { this.type=Type.getAggregateType(type);
  }
  
  private void setAggregate(KeyedListAggregate<Tuple> aggregate)
  { this.aggregate=aggregate;
  }
  
  public void setAutoCreate(boolean val)
  { autoCreate=val;
  }
  
  /**
   * 
   * @param typeURI The TypeURI contained in the XML resource. This is usually
   *   a list type of the resultType.
   */
  public void setTypeURI(URI typeURI)
  { 
    try
    { this.type=Type.resolve(typeURI);
    }
    catch (DataException x)
    { throw new IllegalArgumentException(x);
    }
  }
  
  public void setResourceURI(URI resourceURI)
  { 
    // log.fine("XmlQueryable: uri="+resourceURI);
    this.resourceURI=resourceURI;
  }

  public void setResourceContextURI(URI resourceContextURI)
  { 
    // log.fine("XmlQueryable: uri="+resourceURI);
    this.resourceContextURI=resourceContextURI;
  }
  
  
  
  public void setResource(Resource resource)
  { 
    this.resource=resource;
  }
  
  public Aggregate<Tuple> snapshot()
    throws DataException
  { return getAggregate().snapshot();
  }
  
  
  public synchronized void update(Aggregate<Tuple> snapshot)
    throws DataException,IOException
  { 
    checkInit();
    transaction=Transaction.startContextTransaction(Nesting.PROPOGATE);
    try
    { 
      freeze();
      flush("."+transaction.getId(),snapshot);
      transaction.commit();
    }
    finally
    { transaction.complete();
    }
  }


  synchronized void joinTransaction()
    throws TransactionException
  { 
    Transaction transaction=Transaction.getContextTransaction();
    if (transaction!=null)
    { resourceManager.branch(transaction);
    }
  }
  
  synchronized void flush(String tempSuffix)
    throws DataException,IOException
  { flush(tempSuffix,aggregate);
  }

  synchronized void flush(String tempSuffix,Aggregate<Tuple> aggregate)
    throws DataException,IOException
  {

    if (resource==null)
    { log.fine("Resource is null for "+type);
    }
    Resource tempResource;
    if (tempSuffix!=null)
    {
      tempResource=Resolver.getInstance().resolve
        (URI.create
          (this.resource.getURI().toString()
            +(tempSuffix!=null?tempSuffix:"")
          )
        );
    }
    else
    { tempResource=resource;
    }
    
    DataWriter writer=new DataWriter();
    writer.writeToResource(tempResource, aggregate);
    
    if (tempSuffix!=null)
    {
      Transaction transaction=Transaction.getContextTransaction();
      if (transaction!=null)
      { 
        XmlBranch branch=resourceManager.branch(transaction);
        branch.addResource(tempSuffix);
      
      }
    }
  }
  
  synchronized void commit(String tempSuffix)
    throws IOException
  {
    Resource tempResource
      =Resolver.getInstance().resolve
        (URI.create(this.resource.getURI().toString()+tempSuffix));

    URI backupURI=null;
    int seq=0;
    if (resource.exists())
    {
      while (backupURI==null 
              || Resolver.getInstance().resolve(backupURI).exists()
            )
      {
        backupURI=URI.create
            (resource.getURI().toString()
            +"."+dateFormat.format
              (new Date())
            +"-"+seq
            );
        seq++;
      }
      resource.renameTo
        (backupURI
        );
    }
    
    if (debug)
    {
      log.fine
        ("Renaming "+tempResource.getURI()+" to "+resource.getURI());
    }
    tempResource.renameTo(resource.getURI());
    
    if (backupURI!=null)
    { 
      Resource backupResource=Resolver.getInstance().resolve(backupURI);
      Container container=backupResource.getParent().asContainer();
      Container backupContainer=container.getChild("history").asContainer();
      if (backupContainer!=null && backupResource.exists())
      { backupResource.moveTo(backupContainer.asResource());
      }
      
    }
    
    Transaction transaction=Transaction.getContextTransaction();
    if (transaction!=null)
    { this.lastTransactionId=transaction.getId();
    }
    watcher.reset();
  }
  
  synchronized void rollback(String tempSuffix)
    throws IOException
  {
    Resource tempResource
      =Resolver.getInstance().resolve
        (URI.create(this.resource.getURI().toString()+tempSuffix));
    tempResource.delete();
  }
  

  synchronized void refresh()
    throws IOException
  { watcher.refresh();
  }
  
  
  void add(Tuple t)
  { ((EditableKeyedListAggregate<Tuple>) this.aggregate).add(t);
  }
  
  void remove(Tuple t)
  { ((EditableKeyedListAggregate<Tuple>) this.aggregate).remove(t);
  }
  
  
  
  public synchronized void freeze()
  { 
    if (frozen)
    { 
      if (Transaction.getContextTransaction()!=transaction)
      { 
        try
        { this.wait();
        }
        catch (InterruptedException x)
        { 
          throw new RuntimeException
            ("Wait for lock on "+type.getURI()+" interrupted");
        }
      }
    }
    transaction=Transaction.getContextTransaction();
    frozen=true;
  }
  
  public synchronized void unfreeze()
  { 
    if (Transaction.getContextTransaction()!=transaction)
    { 
      throw new RuntimeException
        ("Lock not owned by transaction "+Transaction.getContextTransaction()
       +", owner is "+transaction
        );
    }
    frozen=false;
    transaction=null;
    this.notify();
  }


  class XmlResourceManager
    extends ResourceManager<XmlBranch>
  {

    @Override
    public XmlBranch createBranch(Transaction transaction)
      throws TransactionException
    { return new XmlBranch();
    }
    
  }
  
  class XmlBranch
  implements Branch
  {
    
    private ArrayList<String> resources=new ArrayList<String>();
    
    private State state=State.STARTED;
    
    public void addResource(String suffix)
    { resources.add(suffix);
    }

    @Override
    public void commit()
    throws TransactionException
    {
      for (String suffix:resources)
      { 
        try
        { 
          synchronized (XmlQueryable.this)
          {
            XmlQueryable.this.freeze();

            XmlQueryable.this.commit(suffix);
        
            // Make -sure- we re-read to verify data and fail for transaction
            //   user
            XmlQueryable.this.refresh();
          }
        }
        catch (IOException x)
        { throw new TransactionException("Error committing '"+suffix+"'",x);
        }
      }
      state=State.COMMITTED;
      // TODO Auto-generated method stub

    }

    @Override
    public void complete()
    {
      try
      {
        if (state!=State.COMMITTED)
        { rollback();
        }
      }
      catch (TransactionException x)
      { log.log(Level.WARNING,"Error rolling back incomplete transaction",x);
      }
      finally
      { XmlQueryable.this.unfreeze();
      }
      state=State.COMPLETED;

    }

    @Override
    public State getState()
    { return state;
    }

    @Override
    public boolean is2PC()
    { return true;
    }

    @Override
    public void prepare()
    throws TransactionException
    { this.state=State.PREPARED;
    }

    @Override
    public void rollback()
    throws TransactionException
    { 

      if (state!=State.COMMITTED)
      { 
        for (String suffix:resources)
        { 
          try
          { XmlQueryable.this.rollback(suffix);
          }
          catch (IOException x)
          { log.log(Level.WARNING,"IOException rolling back '"+suffix+"'",x);
          }
        }
      }


    }
    
    @Override
    public String toString()
    { return super.toString()+": "+XmlQueryable.this.type.getURI();
    }
  }

}
