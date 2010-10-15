//
//Copyright (c) 1998,2007 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data.spi;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Field;
import spiralcraft.data.Sequence;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.Entity;
import spiralcraft.data.access.Schema;
import spiralcraft.data.access.Store;
import spiralcraft.data.access.DeltaTrigger;
import spiralcraft.data.access.Updater;
import spiralcraft.data.core.SequenceField;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;
import spiralcraft.data.transaction.Branch;
import spiralcraft.data.transaction.ResourceManager;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.Transaction.State;
import spiralcraft.data.types.standard.AnyType;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.ArrayUtil;
/**
 * <p>Starting point for building a new type of Store.
 * </p>
 * 
 * <p>The AbstractStore generally represents a set of Queryables that provide
 *   access to data for a set of Types. 
 * </p>
 * 
 * @author mike
 *
 */
public abstract class AbstractStore
  implements Store,Contextual
{
  
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level debugLevel=ClassLog.getInitialDebugLevel(getClass(),null);

  private boolean started;

  protected final Type<?> sequenceType;  
  protected final EquiJoin sequenceQuery;
  
  
  protected Schema schema;
  
  protected long lastTransactionId;
  
  private HashSet<Type<?>> authoritativeTypes=new HashSet<Type<?>>();
  
//  private LinkedHashMap<Type<?>,Queryable<Tuple>> queryables
//    =new LinkedHashMap<Type<?>,Queryable<Tuple>>();

  private LinkedHashMap<Type<?>,EntityBinding> entities
    =new LinkedHashMap<Type<?>,EntityBinding>();
  
  private HashMap<URI,Sequence> sequences;  
  private String name;
  private Focus<Store> focus;
  private Sequence txIdSequence;
  
  private StoreResourceManager resourceManager
    =new StoreResourceManager();
  
  public AbstractStore()
    throws DataException
  {
    sequenceType=Type.resolve("class:/spiralcraft/data/spi/Sequence"); 
      
    sequenceQuery=new EquiJoin();
    sequenceQuery.setSource(new Scan(sequenceType));
    sequenceQuery.setAssignments(".uri=..");
//      sequenceQuery.setDebug(true);
    sequenceQuery.resolve();    
  }
  

  public void setName(String name)
  { this.name=name;
  }
  
  @Override
  public String getName()
  { return name;
  }
  
  /**
   * The id of the last transaction processed by this store. 
   */
  @Override
  public long getLastTransactionId()
  { return lastTransactionId;
  }
  
  /**
   * Indicates that this Store represents the authoritative copy of the data
   *   for the specified Type, and should be used to handle data updates.
   */
  @Override
  public boolean isAuthoritative(Type<?> type)
  { return authoritativeTypes.contains(type);
  }
  
  
  public void setSchema(Schema schema)
  { this.schema=schema;
  }  
  
  
//  @Override
//  public Space getSpace()
//  { return space;
//  }

  @Override
  public boolean containsType(
    Type<?> type)
  { 
    assertStarted();
    return getQueryable(type)!=null;
  }
 
  public void setDebugLevel(Level debugLevel)
  { this.debugLevel=debugLevel;
  }
  

  @Override
  public Sequence getSequence(URI uri)
  {
    return sequences!=null
      ?sequences.get(uri)
      :null
      ;
  }  

  @Override
  public Type<?>[] getTypes()
  {
    Type<?>[] types=new Type[entities.size()];
    int i=0;
    for (EntityBinding entity: entities.values())
    { types[i++]=entity.getEntity().getType();
    }
    return types;
  }
  
  @Override
  public BoundQuery<?,Tuple> query(
    Query query,
    Focus<?> context)
    throws DataException
  { 
    if (context==null)
    { context=focus;
    }
    
    Queryable<Tuple> container=Space.find(context);
    if (container==null)
    { container=this;
    }
    
    // Can be called during bind
    // assertStarted();
    HashSet<Type<?>> typeSet=new HashSet<Type<?>>();
    query.getScanTypes(typeSet);
    Type<?>[] types=typeSet.toArray(new Type[typeSet.size()]);
    
    if (types.length==1)
    {
      Queryable<Tuple> queryable=getQueryable(types[0]);
      if (queryable==null)
      { return null;
      }
      else
      { return queryable.query(query, context);
      }
    }
    
    BoundQuery<?,Tuple> ret=query.solve(context,container);
    ret.resolve();
    if (debugLevel.isDebug())
    { log.debug("returning "+ret+" from query("+query+")");
    }
    return ret;

  }
  
  @Override
  public BoundQuery<?,Tuple> getAll(
    Type<?> type)
    throws DataException
  {
    assertStarted();
    
    Queryable<Tuple> queryable=getQueryable(type);
    if (queryable!=null)
    { return queryable.getAll(type);
    }
    return null;
  }  
  
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  { 
    focus=focusChain.chain(new SimpleChannel<Store>(this,true));
    txIdSequence=createTxIdSequence();
    if (sequences==null)
    { sequences=new HashMap<URI,Sequence>();
    }
    for (EntityBinding binding : entities.values())
    { binding.bind(focus);
    }

    sequences.put(URI.create("_TXID"),txIdSequence);
    return focus;
  }
  
  
  @Override
  public void start()
    throws LifecycleException
  { 
    for (EntityBinding binding: entities.values())
    { binding.start();
    }

    
    if (sequences!=null)
    {
      for (Sequence sequence : sequences.values())
      { sequence.start();
      }
    }
    
    started=true;
  }

  @Override
  public void stop()
    throws LifecycleException
  { 

    started=false;
    // log.fine("Stopping store...");
    if (sequences!=null)
    {
      for (Sequence sequence : sequences.values())
      { sequence.stop();
      }
    }
  }  
  
  /**
   * 
   * @param type The Queryable which handles the specified Type
   * @return
   */
  protected final Queryable<Tuple> getQueryable(Type<?> type)
  { 
    EntityBinding entity=entities.get(type);
    return entity!=null?entity.getQueryable():null;
  }
  
  protected final EntityBinding getEntityBinding(Type<?> type)
  { return entities.get(type);
  }
  
  private void addAuthoritativeType(Type<?> type)
  { 
    if (!authoritativeTypes.contains(type))
    { authoritativeTypes.add(type);
    }
  }
  
  protected List<Queryable<Tuple>> getPrimaryQueryables()
  {
    ArrayList<Queryable<Tuple>> queryables=new ArrayList<Queryable<Tuple>>();
    for (Type<?> type:getTypes())
    { queryables.add(getQueryable(type));
    }
    return queryables;
  }
  
  
  /**
   * <p>Make sure any base-type "union proxies" are set up, to translate a 
   *   Query for the base-type into a union of subtypes.
   * </p>
   * 
   * @param queryable
   */
  protected void addBaseTypes
    (Queryable<Tuple> queryable,Type<?> subtype)
  {
    Type<?> type=subtype.getBaseType();
    while (type!=null)
    { 
      // Set up a queryable for each of the Queryable's base types
      
      EntityBinding targetBinding=entities.get(type);
        
      BaseExtentQueryable<Tuple> baseQueryable;
        
      if (targetBinding==null)
      { 
        baseQueryable=new BaseExtentQueryable<Tuple>(type);
        baseQueryable.addExtent(subtype,queryable);
        
        targetBinding=createEntityBinding(new Entity(type));
        targetBinding.setQueryable(baseQueryable);
        addStandardEntityBinding(targetBinding);
      }
      else if (!(targetBinding.getQueryable() instanceof BaseExtentQueryable<?>))
      {
        // The base extent queryable is already "concrete"
        // This is ambiguous, though. The base extent queryable only
        //   contains the non-subtyped concrete instances of the
        //   base type.
          
        baseQueryable=new BaseExtentQueryable<Tuple>(type);
        baseQueryable.addExtent(type,targetBinding.getQueryable());
        baseQueryable.addExtent(subtype,queryable);

        targetBinding.setQueryable(baseQueryable);
      }
      else
      {

        ((BaseExtentQueryable<Tuple>) targetBinding.getQueryable())
          .addExtent(subtype, queryable);
      }
      type=type.getBaseType();
      
    }
    
  }
   
  
  /**

   * 
   * @param binding
   */
  private void addStandardEntityBinding(EntityBinding binding)
  { 
    Type<?> type=binding.getEntity().getType();
    entities.put(type,binding);
    addSequences(type);
    if (binding.isAuthoritative())
    { addAuthoritativeType(type);
    }
  }

  /**
   * <p>Register a configured EntityBinding with this Store to manage an
   *   access path for a Type.
   * </p> 
   * 
   * @param binding
   */
  protected void addEntityBinding(EntityBinding binding)
  { 
    addStandardEntityBinding(binding);
    if (!(binding.getQueryable() instanceof BaseExtentQueryable))
    { addBaseTypes(binding.getQueryable(),binding.getEntity().getType());
    }
  }
  
  /**
   * Create a new Sequence object that manages the sequence 
   *   for the specified field
   * 
   * @param field
   * @return
   */
  protected abstract Sequence createSequence(Field<?> field);


  /**
   * Create the transactionId sequence
   * 
   * @param field
   * @return
   */
  protected abstract Sequence createTxIdSequence();
  
  /**
   * Construct a new EntityBinding appropriate for the Store implementation.
   * 
   * @param entity
   * @return
   */
  protected EntityBinding createEntityBinding(Entity entity)
  { return new EntityBinding(entity);
  }
  
  protected void assertStarted()
  { 
    if (!started)
    { throw new IllegalStateException("Store has not been started");
    }
  }
  
  private void addSequences(Type<?> subtype)
  {
    if (subtype.getScheme()!=null)
    {
      if (sequences==null)
      { sequences=new HashMap<URI,Sequence>();
      }
      for (Field<?> field : subtype.getScheme().fieldIterable())
      { 
        if (field instanceof SequenceField<?>)
        { 
          if (debugLevel.isDebug())
          { log.fine("added sequence "+field.getURI());
          }
          sequences.put
          (field.getURI()
          ,createSequence(field)
          );
          addAuthoritativeType(subtype);
        }
      }
    }
    
  }  
  

  protected synchronized StoreBranch joinTransaction()
    throws TransactionException
  { 
    Transaction transaction=Transaction.getContextTransaction();
    if (transaction!=null)
    { return resourceManager.branch(transaction);
    }
    else
    { throw new TransactionException("Transaction required");
    }
  }
  
  protected void flushLog(Aggregate<DeltaTuple> deltaList,long txId)
    throws IOException, DataException
  {
    
  }
  
  protected void transactionCommitted(long txId)
  {
  
  }
  
  /**
   * Encapsulates the state of an Entity with a running Store.
   * 
   * @author mike
   *
   */
  public class EntityBinding
    implements Contextual,Lifecycle
  {

    private final Entity entity;
    private Queryable<Tuple> queryable;
    private Updater<DeltaTuple> updater;
    private boolean authoritative;
    
    private DeltaTrigger[] beforeInsert=new DeltaTrigger[0];
    private DeltaTrigger[] afterInsert=new DeltaTrigger[0];
    private DeltaTrigger[] beforeUpdate=new DeltaTrigger[0];
    private DeltaTrigger[] afterUpdate=new DeltaTrigger[0];
    private DeltaTrigger[] beforeDelete=new DeltaTrigger[0];
    private DeltaTrigger[] afterDelete=new DeltaTrigger[0];
    
    public EntityBinding(Entity entity)
    { this.entity=entity;
    }
    
    public Entity getEntity()
    { return entity;
    }
    
    public void setQueryable(Queryable<Tuple> queryable)
    { this.queryable=queryable;
    }
    
    public Queryable<Tuple> getQueryable()
    { return queryable;
    }
    
    public void setUpdater(Updater<DeltaTuple> updater)
    { this.updater=updater;
    }
    
    public Updater<DeltaTuple> getUpdater()
    { return updater;
    }
    
    public boolean isAuthoritative()
    { return authoritative;
    }
    
    public void setAuthoritative(boolean authoritative)
    { this.authoritative=authoritative;
    }
    
    @Override
    public Focus<?> bind(Focus<?> focusChain)
      throws BindException
    { 

      Type.getDeltaType(entity.getType());
      if (updater!=null)
      {
        Focus<?> updaterFocus=updater.bind(focusChain);
      
      
      
        DeltaTrigger[] tupleTriggers=entity.getDeltaTriggers();
        if (tupleTriggers!=null)
        {
          for (DeltaTrigger trigger : tupleTriggers)
          { 
            if (trigger.getTask()!=null)
            { trigger.bind(updaterFocus);
            }
          
            switch (trigger.getWhen())
            {
              case BEFORE:
                if (trigger.isForInsert())
                { beforeInsert=ArrayUtil.append(beforeInsert,trigger);
                }
                if (trigger.isForUpdate())
                { beforeUpdate=ArrayUtil.append(beforeUpdate,trigger);
                }
                if (trigger.isForDelete())
                { beforeDelete=ArrayUtil.append(beforeDelete,trigger);
                }
                break;
              case AFTER:
                if (trigger.isForInsert())
                { afterInsert=ArrayUtil.append(afterInsert,trigger);
                }
                if (trigger.isForUpdate())
                { afterUpdate=ArrayUtil.append(afterUpdate,trigger);
                }
                if (trigger.isForDelete())
                { afterDelete=ArrayUtil.append(afterDelete,trigger);
                }
                break;
            }
          }
        }
        
      }
      else if (entity.getDeltaTriggers()!=null)
      { 
        throw new BindException
          ("Triggers require an Updater in Entity "+entity.getType());
      }
      
      return focusChain;
    }

    
    @Override
    public void start()
      throws LifecycleException
    { 
    }

    @Override
    public void stop()
      throws LifecycleException
    {      
    }
    
    public DeltaTuple beforeInsert(DeltaTuple tuple)
      throws TransactionException
    { return beforeTrigger(beforeInsert,tuple);
    }
    
    public DeltaTuple beforeUpdate(DeltaTuple tuple)
      throws TransactionException
    { return beforeTrigger(beforeUpdate,tuple);
    }

    public DeltaTuple beforeDelete(DeltaTuple tuple)
      throws TransactionException
    { return beforeTrigger(beforeDelete,tuple);
    }

    public void afterInsert(DeltaTuple tuple)
      throws TransactionException
    { afterTrigger(afterInsert,tuple);
    }
  
    public void afterUpdate(DeltaTuple tuple)
      throws TransactionException
    { afterTrigger(afterUpdate,tuple);
    }

    public void afterDelete(DeltaTuple tuple)
      throws TransactionException
    { afterTrigger(afterDelete,tuple);
    }

  
    private DeltaTuple beforeTrigger(DeltaTrigger[] triggers,DeltaTuple tuple)
      throws TransactionException
    { 
      for (DeltaTrigger trigger: triggers)
      { 
        tuple=trigger.trigger();
        if (tuple==null)
        { break;
        }

      }
      return tuple;
    }
    
    private void afterTrigger(DeltaTrigger[] triggers,DeltaTuple tuple)
      throws TransactionException
    {
      for (DeltaTrigger trigger: triggers)
      { trigger.trigger();
      }
    }
  }
  
  class StoreResourceManager
    extends ResourceManager<StoreBranch>
  {

    @Override
    public StoreBranch createBranch(Transaction transaction)
      throws TransactionException
    { return new StoreBranch();
    }
  
  }
  
  protected class StoreBranch
    implements Branch
  {


    private EditableArrayListAggregate<DeltaTuple> deltaList
      =new EditableArrayListAggregate<DeltaTuple>(AnyType.resolve());

    private State state=State.STARTED;

    long txId;
    
    public StoreBranch()
      throws TransactionException
    { 
      
      try
      { txId=txIdSequence.next();
      }
      catch (DataException x)
      { throw new TransactionException("Could not get next transaction id",x);
      }
    }
    
    
    /**
     * Log a Delta
     * 
     * @param tuple
     */
    public void log(DeltaTuple tuple)
      throws DataException
    { 
      deltaList.add
        (new ArrayDeltaTuple
          (tuple.getType().getArchetype()
          ,tuple
          )
        );
    }


    ListAggregate<DeltaTuple> getDeltaList()
    { return deltaList;
    }

    public long getTxId()
    { return txId;
    }
    
    @Override
    public void commit()
    throws TransactionException
    {
      try
      { 
        // XXX Move to prepare when we can save log as temp
        synchronized (AbstractStore.this)
        { AbstractStore.this.flushLog(deltaList,txId);
        }
      }
      catch (IOException x)
      { throw new TransactionException("Error committing",x);
      }
      catch (DataException x)
      { throw new TransactionException("Error committing",x);
      }
      state=State.COMMITTED;

      transactionCommitted(txId);
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
      { 
        if (debugLevel.isTrace())
        { log.trace("Completed StoreBranch "+txId);
        }
        // Release anything here
        
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
    { 
      
      this.state=State.PREPARED;
    }

    @Override
    public void rollback()
    throws TransactionException
    { 

      if (state!=State.COMMITTED)
      { 
      }


    }

    @Override
    public String toString()
    { return super.toString()+": "+AbstractStore.this.toString();
    }
  }

}
