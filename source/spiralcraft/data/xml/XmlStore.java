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

import spiralcraft.command.Command;
import spiralcraft.command.CommandScheduler;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.data.Aggregate;
import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Sequence;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.Snapshot;
import spiralcraft.data.access.Updater;
import spiralcraft.data.access.Entity;
import spiralcraft.data.access.kit.AbstractStore;
import spiralcraft.data.access.kit.AbstractStoreSequence;
import spiralcraft.data.access.kit.EntityBinding;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.sax.DataWriter;
import spiralcraft.data.session.BufferType;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.ResourceSequence;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.Transaction.Nesting;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.types.standard.AnyType;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;
import spiralcraft.task.Scenario;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.util.string.StringPool;
import spiralcraft.util.string.StringUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.util.RetentionPolicy;


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
  
  private Container logContainer;
  
  private XmlQueryable sequenceQueryable
    =new XmlQueryable();
  
  private URI masterURI;
  private Scenario<?,Tuple> subscriber;
  private RetentionPolicy historyRetention
    =new RetentionPolicy();
  
  private final CommandScheduler historyCleaner
    =new CommandScheduler
      (60*60*1000
      ,new Runnable()
        {
          @Override
          public void run()
          { XmlStore.this.cleanHistory();
          }
        }
      );

  private final CommandScheduler updater
    =new CommandScheduler
    (2000
    ,new Runnable()
      {
        @Override
        public void run()
        { 
          if (debugLevel.isFine())
          { log.fine("Running subscription");
          }
          XmlStore.this.triggerSubscriber();
        }
      }
    );
  
  private final StringPool stringPool
    =StringPool.INSTANCE;
  
  public XmlStore()
    throws DataException
  {
    sequenceQueryable.setResultType(sequenceType);
    sequenceQueryable.setResourceURI(URIPool.create("Sequence.xml"));
    sequenceQueryable.setAutoCreate(true);

  }
  
  @Override
  public URI getLocalResourceURI()
  { return baseResourceURI;
  }
  
  @Override
  public void setLocalResourceURI(URI container)
  { setBaseResourceURI(container);
  }
  
  StringPool getStringPool()
  { return stringPool;
  }
  
  @Override
  public Focus<?> bind(Focus<?> focus)
    throws ContextualException
  { 
    super.preBind(focus);
    if (schema!=null)
    {
      try
      { schema.resolve();
      }
      catch (DataException x)
      { throw new BindException("Error resolving schema ",x);
      }
      
      EntityBinding sequenceBinding
        =createEntityBinding(new Entity(sequenceType));
      sequenceBinding.setAccessor(sequenceQueryable);
      addEntityBinding(sequenceBinding);
      
      for (Entity entity: schema.getEntities())
      {
        if (!entity.isAbstract())
        {
          XmlQueryable queryable=new XmlQueryable();
          queryable.setResultType(entity.getType());
          queryable.setResourceURI(URIPool.create(entity.getName()+".data.xml"));
          queryable.setAutoCreate(true);
          xmlQueryables.add(queryable);
          EntityBinding binding=createEntityBinding(entity);
          binding.setAuthoritative(true);
          binding.setAccessor(queryable);
          binding.setUpdater(new XmlUpdater(queryable,binding));   
          if (entity.isDebug())
          { queryable.setLogLevel(Level.TRACE);
          }
          addEntityBinding(binding);
        
          if (debugLevel.isDebug())
          { log.debug
              ("Added XmlQueryable from schema for "+entity.getType().getURI());
          }
        }
      }
    }
   

    if (baseResourceURI==null)
    { baseResourceURI=defaultLocalResourceURI();
    }
    if (baseResourceURI==null)
    { throw new ContextualException("No baseResourceURI configured",getDeclarationInfo());
    }
    focus=super.bind(focus);

    sequenceQueryable.bind(focus);
    if (subscriber!=null)
    { subscriber.bind(focus);
    }
    return focus;
  }
  
  public RetentionPolicy getHistoryRetention()
  { return historyRetention;
  }
  
  public void setSubscriber(Scenario<?,Tuple> subscriber)
  { this.subscriber=subscriber;
  }
  
  public void setMasterRefreshSeconds(int seconds)
  { updater.setPeriod(seconds*1000);
  }
  
  /**
   * The frequency that the store will check the history directory to remove
   *   older files.
   * 
   * @param seconds
   */
  public void setHistoryCleanSeconds(int seconds)
  { historyCleaner.setPeriod(seconds*1000);
  }
  
  public void setBaseResourceURI(URI uri)
  { baseResourceURI=URIUtil.ensureTrailingSlash(uri);
  }

  public void setMasterURI(URI masterURI)
  { this.masterURI=masterURI;
  }
  
  public URI getMasterURI()
  { return masterURI;
  }
  
  public void setQueryables(XmlQueryable[] list)
  { 
    
    for (XmlQueryable queryable:list)
    { 
      xmlQueryables.add(queryable);
      Type<?> subtype=queryable.getResultType();
      EntityBinding entityBinding=createEntityBinding(new Entity(subtype));
      entityBinding.setAuthoritative(true);
      entityBinding.setAccessor(queryable);
      entityBinding.setUpdater(new XmlUpdater(queryable,entityBinding));
      addEntityBinding(entityBinding);
    }
  }

  
  @Override
  public DataConsumer<DeltaTuple> getUpdater(Type<?> type)
    throws DataException
  {
    
    if (type instanceof BufferType)
    { type=type.getArchetype();
    }
    
    EntityBinding binding=getEntityBinding(type);
    
    if (binding==null)
    { return null;
    }
    else if (binding.getUpdater()==null)
    { 
      throw new DataException
        ("Cannot directly update data of type "
        +binding.getEntity().getType().getURI()
        );
    }
    
    return binding.getUpdater();
  }

  @Override
  public void start()
    throws LifecycleException
  {
    try
    { baseResourceURI=Resolver.getInstance().resolve(baseResourceURI).getURI();
    }
    catch (UnresolvableURIException x)
    { throw new LifecycleException("Error resolving "+baseResourceURI,x);
    }


    log.info("Serving data in "+baseResourceURI);
    try
    {
      logContainer
        =Resolver.getInstance().resolve(baseResourceURI.resolve("xlog"))
          .ensureContainer();
    }
    catch (IOException x)
    { 
      throw new LifecycleException
        ("Error resolving transaction log container "
        +baseResourceURI.resolve("xlog")
        ,x
        );
    }
    
    for (XmlQueryable queryable:xmlQueryables)
    { 
      try
      { 
        queryable.setResourceContextURI(baseResourceURI);
        queryable.getAll(queryable.getResultType());    
      }
      catch (DataException x)
      { x.printStackTrace();
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
    
    // Go into data-ready state
    for (XmlQueryable queryable:xmlQueryables)
    { 
      try
      { queryable.checkUpToDate();
      }
      catch (DataException x)
      { x.printStackTrace();
      }
    }
    
    if (subscriber!=null)
    { 
      triggerSubscriber();
      updater.setDelay(true);
      updater.start();
    }
    historyCleaner.start();
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    historyCleaner.stop();
    if (subscriber!=null)
    { updater.stop();
    }
    super.stop();
  }
  
  
  @Override
  protected Sequence createSequence(Field<?> field)
  { return new XmlSequence(field.getURI());
  }

  @Override
  protected Sequence createTxIdSequence()
  { 
    return new ResourceSequence
      (baseResourceURI.resolve(".txid"));
  }
  
  private void cleanHistory()
  { 
    if (debugLevel.isDebug())
    { log.debug("Cleaning history for "+baseResourceURI);
    }
    try
    {
      for (XmlQueryable queryable:xmlQueryables)
      { queryable.cleanHistory(historyRetention);
      }
      
      if (sequenceQueryable!=null)
      { sequenceQueryable.cleanHistory(historyRetention);
      }
    }
    catch (DataException x)
    { log.log(Level.WARNING,"Error cleaning history for "+baseResourceURI,x);
    }
    
  }
  
  
  private void triggerSubscriber()
  { 
    if (subscriber!=null)
    {
      if (debugLevel.isFine())
      { log.fine("Checking subscription...");
      }
      Command<?,?,Tuple> command
        =subscriber.command();
      command.execute();
      Tuple tuple=command.getResult();
      if (tuple!=null)
      { 
        if (debugLevel.isFine())
        { log.fine("Got result "+tuple);
        }
        try
        { update( (Snapshot) tuple.getType().fromData(tuple,null));
        }
        catch (DataException x)
        { 
          log.log
            (Level.WARNING,"XmlStore '"+getName()+"' snapshot update threw"
            +" exception"
            ,x);
        }
      }
      else
      { 
        if (debugLevel.isFine())
        { log.fine("Subscriber returned null");
        }
      }
      if (command.getException()!=null)
      { 
        log.log
          (Level.WARNING,"Subscriber for store '"+getName()+"' threw exception"
          ,command.getException()
          );
      }
    }
  }
  

  @Override
  public void update(Snapshot snapshot)
  {
    Transaction transaction
      =Transaction.startContextTransaction(Nesting.PROPOGATE);
    try
    { 

      ArrayList<Type<?>> types=new ArrayList<Type<?>>();
      
      for (Aggregate<Tuple> aggregate : snapshot.getData())
      { 
        Type<?> type=aggregate.getType().getContentType();
        Queryable<?> queryable=getQueryable(type);
        if (queryable==null || !(queryable instanceof XmlQueryable))
        { log.warning("Ignoring snapshot of "+type.getURI());
        }
        else
        { 
          
          StoreBranch tx=joinTransaction();
          ((XmlQueryable) queryable).joinTransaction().setStoreBranch(tx);
          try
          { ((XmlQueryable) queryable).updateFromSnapshot(aggregate);
          }
          catch (DataException x)
          { 
            log.log
              (Level.WARNING,"Failed to deliver subscription to "+type.getURI()
              ,x);
          }
          catch (IOException x)
          { 
            log.log
            (Level.WARNING,"Failed to deliver subscription to "+type.getURI()
            ,x);
          }
          types.add(type);
        }
        
      }
      
      if (!types.isEmpty())
      { onReload(types.toArray(new Type<?>[types.size()]));
      }
      
      transaction.commit();
    }
    catch (TransactionException x)
    { 
      x.printStackTrace();
      transaction.rollbackOnComplete();
    }
    finally
    { transaction.complete();
    }
      
    lastTransactionId=snapshot.getTransactionId();
  }

  @Override
  public Snapshot snapshot(long transactionId)
    throws DataException
  {
    if (transactionId==0 || lastTransactionId>transactionId)
    {
      EditableArrayTuple snapshot=new EditableArrayTuple(Snapshot.TYPE);
      
      // Note- do not set to 0 here or entire dataset will be sent repeatedly
      snapshot.set("transactionId",(lastTransactionId!=0)?lastTransactionId:1);
      
      EditableArrayListAggregate<Aggregate<Tuple>> data
        =new EditableArrayListAggregate<Aggregate<Tuple>>
          (Type.resolve(AnyType.TYPE_URI+".list.list")
          );
      for (XmlQueryable queryable:xmlQueryables)
      {
        if (transactionId==0
            || queryable.getLastTransactionId()>transactionId
            )
        { data.add(queryable.snapshot());
        }
      }
      snapshot.set("data",data);
      return Snapshot.TYPE.fromData(snapshot,null);
    }
    else
    { return null;
    }
  }


  
  @Override
  protected synchronized void flushLog(Aggregate<DeltaTuple> aggregate,long txId)
    throws DataException,IOException
  {
    if (aggregate.size()>0)
    {
      
      String txidHex=StringUtil.prepad(Long.toHexString(txId),'0',12);
      
      String bundle1=txidHex.substring(0,9)+".dir";
      
      Container bundle1Container
        =logContainer.getChild(bundle1).ensureContainer();
      
      Resource logResource
        =bundle1Container.getChild
          ("tx"+txidHex+".delta.xml");

      DataWriter writer=new DataWriter();
      writer.writeToResource(logResource, aggregate);
    }
  }
  
  class XmlUpdater
    extends Updater<DeltaTuple>
  {
    private XmlQueryable queryable;
    private EntityBinding entityBinding;
    
    public XmlUpdater
      (XmlQueryable queryable,EntityBinding binding
      )
    { 
      super();
      this.queryable=queryable;
      this.entityBinding=binding;
      this.debug=XmlStore.this.debugLevel.isDebug();
      setFieldSet(binding.getEntity().getType().getFieldSet());

    }
    
    @Override
    public Focus<?> bind(Focus<?> context)
      throws BindException
    { 
      context=super.bind(context);
      queryable.bindDRI(context);
      return context;
    }
    
    @Override
    public void dataInitialize(FieldSet fieldSet)
      throws DataException
    { 
      super.dataInitialize(fieldSet);

      // Make sure queryable has had a chance to init.
      queryable.checkUpToDate();
      StoreBranch tx=joinTransaction();
      queryable.joinTransaction().setStoreBranch(tx);
      
    }
    
    @Override
    public void dataAvailable(DeltaTuple tuple)
      throws DataException
    {
      super.dataAvailable(tuple);
      localChannel.push(tuple);
      try
      {
        if (tuple.getOriginal()==null && !tuple.isDelete())
        { 
          // New case
          
          // Run triggers
          tuple=entityBinding.beforeInsert(tuple);
          if (tuple==null)
          { return;
          }

          joinTransaction().log(tuple);
          queryable.joinTransaction().insert(tuple);
          entityBinding.afterInsert(tuple);
        }
        else if (!tuple.isDelete())
        { 
          // Run trigger
          tuple=entityBinding.beforeUpdate(tuple);
          if (tuple==null)
          { return;
          }

          joinTransaction().log(tuple);
          queryable.joinTransaction().update(tuple);
          entityBinding.afterUpdate(tuple);
        }
        else
        { 
          // Delete case
          tuple=entityBinding.beforeDelete(tuple);
          if (tuple==null)
          { return;
          }

          joinTransaction().log(tuple);
          queryable.joinTransaction().delete(tuple);
          entityBinding.afterDelete(tuple);
        }

      }
      finally
      { localChannel.pop();
      }
    }
    

    

    

    

    
    @Override
    public void dataFinalize()
      throws DataException
    { 
      super.dataFinalize();         

    }
  }
  
  class XmlSequence
    extends AbstractStoreSequence
  {

    
    public XmlSequence (URI uri)
    { super(XmlStore.this,uri,sequenceQueryable);
    }

    @Override
    protected BoundQuery<?,Tuple> bindQuery(Query sequenceQuery)
      throws DataException
    { return sequenceQueryable.query(sequenceQuery,uriFocus);
    }
    
    @Override
    protected void updateInTx(DeltaTuple dt)
      throws TransactionException,DataException
    {
      StoreBranch tx=joinTransaction();
      sequenceQueryable.joinTransaction().setStoreBranch(tx);
      sequenceQueryable.joinTransaction().update(dt);
      joinTransaction().log(dt);
    }
    
    @Override
    protected void insertInTx(DeltaTuple dt)
      throws TransactionException,DataException
    {
      StoreBranch tx=joinTransaction();
      sequenceQueryable.joinTransaction().setStoreBranch(tx);
      sequenceQueryable.joinTransaction().insert(dt);      
      joinTransaction().log(dt);    
    }
  }
  
  
}
