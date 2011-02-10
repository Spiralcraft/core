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
import spiralcraft.common.LifecycleException;
import spiralcraft.data.Aggregate;
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
import spiralcraft.data.access.Snapshot;
import spiralcraft.data.access.Updater;
import spiralcraft.data.access.Entity;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.sax.DataWriter;
import spiralcraft.data.session.BufferType;
import spiralcraft.data.spi.AbstractStore;
import spiralcraft.data.spi.ArrayDeltaTuple;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.EntityBinding;
import spiralcraft.data.spi.ResourceSequence;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.Transaction.Nesting;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.types.standard.AnyType;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.Level;
import spiralcraft.task.Scenario;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
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
  
  public XmlStore()
    throws DataException
  {
    sequenceQueryable.setResultType(sequenceType);
    sequenceQueryable.setResourceURI(URI.create("Sequence.xml"));
    sequenceQueryable.setAutoCreate(true);

  }
  
  @Override
  public URI getLocalResourceURI()
  { return baseResourceURI;
  }
  
  @Override
  public Focus<?> bind(Focus<?> focus)
    throws BindException
  { 
    if (schema!=null)
    {
      try
      { schema.resolve();
      }
      catch (DataException x)
      { throw new BindException("Error resolving schema ",x);
      }
      
      for (Entity entity: schema.getEntities())
      {
        XmlQueryable queryable=new XmlQueryable();
        queryable.setResultType(entity.getType());
        queryable.setResourceURI(URI.create(entity.getName()+".data.xml"));
        queryable.setAutoCreate(true);
        xmlQueryables.add(queryable);
        EntityBinding binding=createEntityBinding(entity);
        binding.setAuthoritative(true);
        binding.setQueryable(queryable);
        binding.setUpdater(new XmlUpdater(queryable,binding));   
        if (entity.isDebug())
        { queryable.setLogLevel(Level.TRACE);
        }
        addEntityBinding(binding);
        
        if (debugLevel.isDebug())
        { log.debug("Added XmlQueryable from schema");
        }        
      }
    }
    
    
    focus=super.bind(focus);

    for (XmlQueryable queryable:xmlQueryables)
    { queryable.bind(focus);
    }
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
  { baseResourceURI=uri;
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
      entityBinding.setQueryable(queryable);
      entityBinding.setUpdater(new XmlUpdater(queryable,entityBinding));
      addEntityBinding(entityBinding);
    }
  }

  
  @Override
  public DataConsumer<DeltaTuple> getUpdater(
    Type<?> type,Focus<?> focus)
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
      { onReload(types.toArray(new Type[types.size()]));
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
      Resource logResource
        =logContainer.getChild("tx"+txId+".delta.xml");

      DataWriter writer=new DataWriter();
      writer.writeToResource(logResource, aggregate);
    }
  }
  
  class XmlUpdater
    extends Updater<DeltaTuple>
  {
    private XmlQueryable queryable;
    private EntityBinding entityBinding;
    

    
    private ArrayList<BoundQuery<?,Tuple>> uniqueQueries
      =new ArrayList<BoundQuery<?,Tuple>>();
    private ArrayList<Key<?>> uniqueKeys
      =new ArrayList<Key<?>>();
    
    
    
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
      Type<?> type=queryable.getResultType();
      
      context=super.bind(context);
      
      try
      {
      
        for (Key<?> key: type.getKeys())
        {
          if (key.isUnique() || key.isPrimary())
          { 
            // Create queries for unique keys and associate the Key 
            //   with the query via a parallel list for error reporting.
            uniqueQueries.add(queryable.query(key.getQuery(),localFocus));
            uniqueKeys.add(key);
          }
        }
      }
      catch (DataException x)
      { throw new BindException("Error binding DRI rules for "+type.getURI()); 
      }
      
      
      return context;
    }
    
    @Override
    public void dataInitialize(FieldSet fieldSet)
      throws DataException
    { 
      super.dataInitialize(fieldSet);

      // Make sure queryable has had a chance to init.
      queryable.getAggregate();
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
          checkInsertIntegrity(tuple);

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
          tuple=checkUpdateIntegrity(tuple);

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
    
    /**
     * Check data integrity constraints for an insert
     * 
     * @param tuple
     * @throws DataException
     */
    private void checkInsertIntegrity(DeltaTuple tuple)
      throws DataException
    {      
      // Check unique keys

      int i=0;
      for (BoundQuery<?,Tuple> query: uniqueQueries)
      {
        SerialCursor<Tuple> cursor=query.execute();
        try
        {
          if (cursor.next())
          { 
            if (debug)
            { 
              log.fine
                ("Unique conflict on add: "+tuple+":"+uniqueKeys.get(i));
            }
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
    

    
    /**
     * Check data integrity constraints for an update
     * 
     * @param tuple
     * @throws DataException
     */
    private DeltaTuple checkUpdateIntegrity(DeltaTuple tuple)
      throws DataException
    {      
      // Check unique keys
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
              Tuple newOriginal=queryable.getStoreVersion(tuple.getOriginal());
              if (!cursor.getTuple().equals(newOriginal))
              {
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
              else
              { 
                // Deal with concurrent update- overwrite for now
                tuple=tuple.rebase(newOriginal);
              }
            }
          }
        }
        finally
        { cursor.close();
        }
        i++;
        
      }
      return tuple;
    }
    

    
    @Override
    public void dataFinalize()
      throws DataException
    { 
             

    }
  }
  
  class XmlSequence
    implements Sequence
  {

    private int increment;
    private volatile long next;
    private volatile long stop;
    private BoundQuery<?,Tuple> boundQuery;
    private Focus<URI> uriFocus;
    private URI uri;
    
    public XmlSequence (URI uri)
    { 
      this.uri=uri;
      uriFocus=new SimpleFocus<URI>(new SimpleChannel<URI>(uri,true));
    }

    @Override
    public void start()
      throws LifecycleException
    {
      try
      {
        boundQuery
          =sequenceQueryable.query(sequenceQuery,uriFocus);
        if (boundQuery==null)
        { 
          throw new LifecycleException
            ("Got null for sequence query "+sequenceQuery);
        }
      }
      catch (DataException x)
      { 
        throw new LifecycleException
          ("Error binding sequence query for "+uri,x);
      }
    }
    
    @Override
    public void stop()
      throws LifecycleException
    {
      try
      {
        deallocate();
      }
      catch (DataException x)
      { 
        throw new LifecycleException
          ("Error deallocating sequence "+uri,x);
      }
    }

    public void deallocate()
      throws DataException
    {
      synchronized(sequenceQueryable)
      {
        Transaction.startContextTransaction(Nesting.ISOLATE);
        try
        {
          SerialCursor<Tuple> result=boundQuery.execute();
          EditableTuple row=null;
          Tuple oldRow=null;
          
          try
          {
            if (!result.next())
            {
            }
            else
            {
              oldRow=result.getTuple().snapshot();
              row=new EditableArrayTuple(oldRow);
              row.set("nextValue",next);
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
          
  
          if (oldRow!=null && row!=null)
          {
          
            StoreBranch tx=joinTransaction();
            sequenceQueryable.joinTransaction().setStoreBranch(tx);
            
            DeltaTuple dt=new ArrayDeltaTuple(oldRow,row);
            sequenceQueryable.joinTransaction().update(dt);
            joinTransaction().log(dt);
            Transaction.getContextTransaction().commit();
          
          
          }
        }
        finally
        { Transaction.getContextTransaction().complete();
        }
        
      }

    }
    
    public void allocate()
      throws DataException
    {
      synchronized(sequenceQueryable)
      {

        Transaction.startContextTransaction(Nesting.ISOLATE);
        try
        {
          SerialCursor<Tuple> result=boundQuery.execute();
  
          EditableTuple row=null;
          Tuple oldRow=null;
          
          try
          {
            if (!result.next())
            {
              row=new EditableArrayTuple(sequenceType);
              
              row.set("uri",uri);
              row.set("nextValue",200L);
              row.set("increment",100);
  
              next=100;
              stop=200;
              increment=100;
  
            }
            else
            {
              oldRow=result.getTuple().snapshot();
              row=new EditableArrayTuple(oldRow);
              
              next=(Long) row.get("nextValue");
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
        

          StoreBranch tx=joinTransaction();
          sequenceQueryable.joinTransaction().setStoreBranch(tx);
          DeltaTuple dt=new ArrayDeltaTuple(oldRow,row);
          if (oldRow!=null)
          { sequenceQueryable.joinTransaction().update(dt);   
          }
          else
          { sequenceQueryable.joinTransaction().insert(dt);   
          }
          joinTransaction().log(dt);

          Transaction.getContextTransaction().commit();
        }
        finally
        { Transaction.getContextTransaction().complete();
        }
        
      }
    }
    
    
    
    @Override
    public synchronized Long next()
      throws DataException
    {
      if (next==stop)
      { allocate();
      }
      return next++;
    }
  }
  
  
}
