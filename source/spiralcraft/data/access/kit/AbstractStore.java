//
//Copyright (c) 1998,2011 Michael Toth
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
package spiralcraft.data.access.kit;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.common.declare.Declarable;
import spiralcraft.common.declare.DeclarationInfo;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Field;
import spiralcraft.data.Sequence;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.access.Entity;
import spiralcraft.data.access.ExportSequence;
import spiralcraft.data.access.ImportSequence;
import spiralcraft.data.access.Schema;
import spiralcraft.data.access.Store;
import spiralcraft.data.access.StoreService;
import spiralcraft.data.core.SequenceField;

import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;
import spiralcraft.data.spi.ArrayDeltaTuple;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.spi.ListAggregate;
import spiralcraft.data.transaction.Branch;
import spiralcraft.data.transaction.ResourceManager;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.Transaction.State;
import spiralcraft.data.types.standard.AnyType;

import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

/**
 * <p>Starting point for building a new type of Store.
 * </p>
 * 
 * <p>The AbstractStore holds a set of Queryables that provide
 *   access to data for a set of Types. 
 * </p>
 * 
 * @author mike
 *
 */
public abstract class AbstractStore
  implements Store,Declarable
{
  
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level debugLevel=ClassLog.getInitialDebugLevel(getClass(),null);

  private Space space;
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
  
  private StoreService[] services;
  
  private String name;
  private Focus<AbstractStore> focus;
  private Sequence txIdSequence;
  
  private final LinkedHashSet<Long> txInProgress
    =new LinkedHashSet<Long>();
  private long minOpenTx;
  
  private boolean pub=true;
  private DeclarationInfo declarationInfo;
  
  protected ResourceManager<? extends StoreBranch> resourceManager
    =new ResourceManager<StoreBranch>()
  {
    @Override
    public StoreBranch createBranch(Transaction transaction)
      throws TransactionException
    { return new StoreBranch();
    }
  };
  
  
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
  

  @Override
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
  
  @Override
  public void setPublic(boolean pub)
  { this.pub=pub;
  }
  
  @Override
  public boolean isPublic()
  { return pub;
  }
  
  @Override
  public Schema getSchema()
  { return schema;
  }
  
  public void setSchema(Schema schema)
  { this.schema=schema;
  }  
  
  public void setServices(StoreService[] services)
  { this.services=services; 
  }
  


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
  
  public void setLogLevel(Level logLevel)
  { this.debugLevel=logLevel;
  }

  @Override
  public Sequence getSequence(URI uri)
    throws DataException
  {
    Sequence sequence
      =sequences!=null
      ?sequences.get(uri)
      :null
      ;
      
    if (sequence==null)
    { sequence=space.getSequence(uri);
    }
    return sequence;
  }  

  @Override
  public Type<?>[] getTypes()
  {
    Type<?>[] types=new Type<?>[entities.size()];
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
    BoundQuery<?,Tuple> ret=solve(query,context);
    if (ret==null)
    { 
      ret=query.solve(context,this);
    }
    ret.resolve();
    
    if (debugLevel.isDebug())
    { log.debug("returning "+ret+" from query("+query+")");
    }
    return ret;
    
  }
  
  
  @Override
  public BoundQuery<?,Tuple> solve(
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
    
    HashSet<Type<?>> typeSet=new HashSet<Type<?>>();
    query.getAccessTypes(typeSet);
    Type<?>[] types=typeSet.toArray(new Type<?>[typeSet.size()]);
    
    if (services!=null)
    {
      for (StoreService service:services)
      { 
        BoundQuery<?,Tuple> ret=service.handleQuery(query,types,context);
        if (ret!=null)
        { return ret;
        }
      }
    }
    
    if (types.length==1)
    {
      Queryable<Tuple> queryable=getQueryable(types[0]);
      if (queryable==null)
      { return null;
      }
      else
      { return queryable.solve(query, context);
      }
    }
    return null;

  }
  
  @Override
  public BoundQuery<?,Tuple> getAll(
    Type<?> type)
    throws DataException
  {
    //assertStarted();
    
    Queryable<Tuple> queryable=getQueryable(type);
    if (queryable!=null)
    { return queryable.getAll(type);
    }
    return null;
  }  
  
  
  protected void preBind(Focus<?> focusChain)
  { space=LangUtil.findInstance(Space.class,focusChain);
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  { 
    focus=focusChain.chain(LangUtil.constantChannel(this));
    
    bindServices(focus);
    
    txIdSequence=createTxIdSequence();
    if (sequences==null)
    { sequences=new HashMap<URI,Sequence>();
    }
    for (EntityBinding binding : entities.values())
    { binding.bind(focus);
    }

    sequences.put(URIPool.create("_TXID"),txIdSequence);
    return focus;
  }
  
  private void bindServices(Focus<?> focusChain)
    throws ContextualException
  {
    if (services!=null)
    { 
      for (StoreService service:services)
      { 
        Focus<?> serviceFocus=service.bind(focusChain);
        if (serviceFocus!=focusChain)
        { focusChain.addFacet(serviceFocus);
        }
      }
    }
  }
  
  @Override
  public void setDeclarationInfo(
    DeclarationInfo declarationInfo)
  { this.declarationInfo=declarationInfo;
  }

  @Override
  public DeclarationInfo getDeclarationInfo()
  { return declarationInfo;
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    
    if (sequences!=null)
    {
      for (Sequence sequence : sequences.values())
      { 
        if (sequence instanceof Lifecycle)
        { ((Lifecycle) sequence).start();
        }
      }
    }
    
    for (EntityBinding binding : entities.values())
    { binding.start();
    }    
    
    Lifecycler.start(services);
    
    started=true;
  }

  @Override
  public void stop()
    throws LifecycleException
  { 

    started=false;
    // log.fine("Stopping store...");
    
    Lifecycler.stop(services);

    for (EntityBinding binding : entities.values())
    { binding.stop();
    }    

    if (sequences!=null)
    {
      for (Sequence sequence : sequences.values())
      { 
        if (sequence instanceof Lifecycle)
        { ((Lifecycle) sequence).stop();
        }
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
        
        Entity schemaEntity=schema.getEntity(type);
        if (schemaEntity==null)
        { schemaEntity=new Entity(type);
        }
        targetBinding=createEntityBinding(schemaEntity);
        targetBinding.setAccessor(baseQueryable);
        addStandardEntityBinding(targetBinding);
        if (debugLevel.isDebug())
        { 
          log.fine("Added new abstract queryable for "
                  +type.getURI()+" with extent for "+subtype.getURI()
                  );
        }
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

        targetBinding.setConcreteAccessor(targetBinding.getAccessor());
        targetBinding.setAccessor(baseQueryable);
        if (debugLevel.isDebug())
        { 
          log.fine("Added new abstract queryable for "
                  +type.getURI()+" with extent for "+subtype.getURI()
                  +" and for concrete instances"
                  );
        }
      }
      else
      {

        ((BaseExtentQueryable<Tuple>) targetBinding.getQueryable())
          .addExtent(subtype, queryable);
        if (debugLevel.isDebug())
        { 
          log.fine("Added new extent to abstract queryable "
                  +type.getURI()+" for "+subtype.getURI()
                  );
        }
        
      }
      type=type.getBaseType();
      
    }
    
  }
   
  
  /**

   * 
   * @param ing
   */
  private void addStandardEntityBinding(EntityBinding binding)
  { 
    Type<?> type=binding.getEntity().getType();
    entities.put(type,binding);
    addSequences(binding,type);
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
  
  private void addSequences(EntityBinding binding,Type<?> subtype)
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
          
          if (binding.getEntity().getAttribute(ImportSequence.class) ==null)
          {
            sequences.put
              (field.getURI()
              ,createSequence(field)
              );
            if (binding.getEntity().getAttribute(ExportSequence.class) !=null)
            { space.exportSequence(field.getURI(),sequences.get(field.getURI()));
            }
          }
          // // Can't do this here- just because the sequence is in the subtype
          // //   doesn't mean we can write to an abstract subtype. This will
          // //   break updates if an abstract entity is concrete in a different
          // //   store.
          // addAuthoritativeType(subtype);
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
  
  
  protected void transactionCommitted(long transactionId)
  { 
    lastTransactionId=transactionId;
    if (debugLevel.isTrace())
    { 
      log.fine
        ("StoreBranch TX "+transactionId+" committed");
    }
  }
  
  public void onReload(Type<?>[] types)
  {
    if (services!=null)
    {
      for (StoreService service:services)
      { service.onReload(types);
      }
    }    
  }
  
  protected URI defaultLocalResourceURI()
    throws ContextualException
  {
    String name=this.getName();
    if (name==null && schema!=null)
    { name=schema.getName();
    }
    else
    { name="default";
    }
    
    Resource dataResource=null;
    try
    {
      dataResource
        =Resolver.getInstance().resolve("context://data");
      if (dataResource.exists())
      { return dataResource.asContainer().ensureChildContainer(name+".store").getURI();
      }
      else
      { 
        throw new ContextualException
          ("Data context does not exist: "+dataResource.getURI(),this.getDeclarationInfo());
      }
    }
    catch (IOException x)
    { 
      throw new ContextualException
        ("Data context does not exist: "
          +(dataResource!=null?dataResource.getURI():"context://data")
        ,this.getDeclarationInfo()
        ,x
        );
    }
  }
  
  public class StoreBranch
    implements Branch
  {


    private EditableArrayListAggregate<DeltaTuple> deltaList
      =new EditableArrayListAggregate<DeltaTuple>(AnyType.resolve());

    private State state=State.STARTED;
    private final Set<Long> txOpen;
    private final long earliestOpenTx=minOpenTx;

    long txId;
    
    public StoreBranch()
      throws TransactionException
    { 
      synchronized (txInProgress)
      {
        txOpen=new HashSet<Long>(txInProgress);
        try
        { txId=txIdSequence.next();
        }
        catch (DataException x)
        { throw new TransactionException("Could not get next transaction id",x);
        }
        txInProgress.add(txId);
        if (minOpenTx==0)
        { minOpenTx=txId;
        }
      }
    }
    
    
    public boolean isVisible(long txId)
    { 
      return 
        (earliestOpenTx==0 && txId<=this.txId)
        || txId==this.txId 
        || txId<earliestOpenTx
        || (txId<this.txId && !txOpen.contains(txId))
        ;
        
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
        (ArrayDeltaTuple.copy(tuple)
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
        synchronized (txInProgress)
        {
          // Release anything here
          txInProgress.remove(txId);
          if (minOpenTx==this.txId)
          { 
            if (!txInProgress.isEmpty())
            { minOpenTx=txInProgress.iterator().next();
            }
          }
          
        }
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
