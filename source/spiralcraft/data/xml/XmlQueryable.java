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

import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.core.ProjectionImpl;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.spi.AbstractAggregateQueryable;
import spiralcraft.data.spi.AbstractStore;
import spiralcraft.data.spi.ArrayDeltaTuple;
import spiralcraft.data.spi.ArrayJournalTuple;
import spiralcraft.data.spi.EditableKeyedListAggregate;
import spiralcraft.data.spi.KeyedListAggregate;

import spiralcraft.data.sax.DataReader;
import spiralcraft.data.sax.DataWriter;
import spiralcraft.data.transaction.Branch;
import spiralcraft.data.transaction.ResourceManager;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.Transaction.Nesting;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.Transaction.State;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Field;
import spiralcraft.data.Identifier;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.Key;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;


import spiralcraft.util.Path;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.filters.PatternFilter;
import spiralcraft.vfs.util.RetentionPolicy;
import spiralcraft.vfs.watcher.ResourceWatcher;
import spiralcraft.vfs.watcher.WatcherHandler;


import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.IOException;

import org.xml.sax.SAXException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;
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
  implements Contextual
{
  private final ClassLog log
    =ClassLog.getInstance(XmlQueryable.class);
  
  private Level logLevel
    =ClassLog.getInitialDebugLevel(XmlQueryable.class,null);
  
  private SimpleDateFormat dateFormat
    =new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
  
  private Resource resource;
  private URI resourceURI;
  private URI resourceContextURI;
  
  private ResourceWatcher watcher;
  
  private Type<?> type;
  
  private KeyedListAggregate<Tuple> aggregate;
//  private KeyedListAggregate<Tuple> snapshot;
  
  private Exception exception;
  
  private boolean autoCreate;
  
  
  private int freezeCount;
  private long lastTransactionId;
  
  private XmlResourceManager resourceManager
    =new XmlResourceManager();
  
  private volatile Transaction transaction;
  private ThreadLocalChannel<Tuple> localData;
  private Focus<Tuple> localFocus;
  private BoundQuery<?,Tuple> originalQuery;
  private final Lock setLock=new ReentrantLock(true);
  private XmlStore store;
  
  public void setLogLevel(Level logLevel)
  { this.logLevel=logLevel;
  }
  
  public long getLastTransactionId()
  { return lastTransactionId;
  }
  
  private WatcherHandler handler
    =new WatcherHandler()
    {
      @Override
      public int handleUpdate(Resource resource)
      {
        
        try
        {
          exception=null;
          if (logLevel.isDebug())
          { log.debug("Resource "+resource.getURI()+" changed");
          }
          
          reload();
            
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
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    this.store=LangUtil.findInstance(XmlStore.class,focusChain);
    localData=new ThreadLocalChannel<Tuple>
      (DataReflector.<Tuple>getInstance(getResultType()));
    localFocus=focusChain.chain(localData);
    Key<?> primaryKey=getResultType().getPrimaryKey();
    if (primaryKey!=null)
    { 
      try
      { originalQuery=query(primaryKey.getQuery(), localFocus);
      }
      catch (DataException x)
      { throw new BindException("Error resolving identity query",x);
      }
    }
    else
    { 
      try
      {

        ArrayList<String> fields=new ArrayList<String>();
        for (Field<?> field: getResultType().getFieldSet().fieldIterable())
        { 
          if ( !field.isTransient() )
          { fields.add(field.getName());
          }
        }
        String[] fieldNames=fields.toArray(new String[fields.size()]);
        
        ProjectionImpl<Tuple> equijoin
          =new ProjectionImpl<Tuple>
            (getResultType().getFieldSet()
            ,fieldNames
            );
        originalQuery=query(equijoin.getIdentityQuery(), localFocus);
        //originalQuery.setDebugLevel(Level.FINE);
      }
      catch (DataException x)
      { throw new BindException("Error resolving identity query",x);
      }
      
      
    }
    return focusChain;

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
              writeData(resource,aggregate);
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
  
  
  /**
   * Reload the base data from the local copy into the store. This is not
   *   transactional.
   * 
   * @param orig
   */
  @SuppressWarnings("unchecked")
  protected synchronized void reload()
    throws DataException,IOException,SAXException
  { 
    Transaction newTransaction=null;
    if (Transaction.getContextTransaction()==null)
    { newTransaction=Transaction.startContextTransaction(Nesting.PROPOGATE);
    }
    try
    {
      
      freeze();
      try
      {
        if (logLevel.isFine())
        { log.fine("Base resource modified, reloading from "+resource.getURI());
        }

        DataReader reader=new DataReader();
        Aggregate<Tuple> orig
          =(Aggregate<Tuple>) reader.readFromResource
            (resource, type);

        aggregate=new EditableKeyedListAggregate<Tuple>(orig.getType());
        for (Tuple t: orig)
        {
          ArrayJournalTuple nt=new ArrayJournalTuple(t);
          add(nt);
        }
        if (store!=null)
        { store.onReload(new Type[] {getResultType()});
        }
        if (newTransaction!=null)
        { newTransaction.commit();
        }
        if (logLevel.isFine())
        { log.fine("Reloaded "+orig.size()+" tuples from "+resource.getURI());
        }
      }
      finally
      { unfreeze();
      }
      
    
    }
    finally
    { 
      if (newTransaction!=null)
      { newTransaction.complete();
      }
    }
  }
  
  @Override
  protected synchronized Aggregate<Tuple> getAggregate()
    throws DataException
  { 
    checkInit();

    if (freezeCount==0)
    {
      if (logLevel.isFine())
      { log.fine("Checking resource "+resource.getURI());
      }
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
  

  /**
   * Merges a more recent store change with the specified Tuple, and throws
   *   an exception if an update conflict was detected.
   * 
   * @param dt
   * @return
   * @throws DataException
   */
  DeltaTuple merge(DeltaTuple dt)
    throws DataException
  {

    // Must find the old copy
    JournalTuple original=(JournalTuple) dt.getOriginal();
    Tuple latest=getStoreVersion(dt);
    if (latest!=original)
    { 
      if (logLevel.isFine())
      { log.fine("Rebasing "+dt+" to "+latest);
      }
      return dt.rebase(latest);
    }
    else
    { return dt;
    }
  }
  
  /**
   * Return the most recent store version that has the same primary
   *   key as the specified Tuple
   * 
   * @param t
   * @return
   */
  Tuple getStoreVersion(Tuple t)
    throws DataException
  { 
    
    

    localData.push(t);
    try
    {
      Tuple ret=t;
      
      // Find a more certain original
      SerialCursor<Tuple> cursor=originalQuery.execute();
      try
      {
        if (!cursor.next())
        {
          // Old one has been deleted, or never existed
          ret=null;
        }
        else
        { ret=cursor.getTuple().snapshot();
        }
        if (cursor.next())
        { 
          throw new DataException
            ("Cardinality violation: duplicate "+cursor.getTuple()+" <> "+ret);
          
        }
        return ret;
      }
      finally
      { cursor.close();
      }
    }
    finally
    { localData.pop();
    }
  }

  
  /**
   * Update the base cache from a snapshot. Will be flushed by the next
   *   transaction commit
   * 
   * @param snapshot
   * @throws DataException
   * @throws IOException
   */
  synchronized void updateFromSnapshot(Aggregate<Tuple> snapshot)
    throws DataException,IOException
  { 
    
    checkInit();
    freeze();
    try
    {
      aggregate=new EditableKeyedListAggregate<Tuple>(snapshot.getType());
      for (Tuple t: snapshot)
      {
        ArrayJournalTuple nt=new ArrayJournalTuple(t);
        add(nt);
      }
      if (logLevel.isFine())
      { log.fine("Updated base cache for "+resource.getURI()+" from snapshot");
      }
    }
    finally
    { unfreeze();
    }

  }


  XmlBranch joinTransaction()
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
  

  
  synchronized void flushResource()
    throws DataException,IOException
  { flushResource(aggregate);
  }

  synchronized void flushResource(Aggregate<Tuple> aggregate)
    throws DataException,IOException
  {
    long txId;
    String tempSuffix=null;
    if (Transaction.getContextTransaction()!=null)
    { 
      txId=joinTransaction().txId;
      tempSuffix=".tx"+txId;
    }
    else
    { throw new DataException("Transaction required for "+resource.getURI());
    }
    
    if (resource==null)
    { 
      if (logLevel.isFine())
      { log.fine("Resource is null for "+type);
      }
    }
    Resource tempResource;

    tempResource=Resolver.getInstance().resolve
      (URI.create
        (this.resource.getURI().toString()
          +(tempSuffix!=null?tempSuffix:"")
        )
      );
    
    if (tempResource.exists())
    { throw new IOException
        ("Resource "+tempResource.getURI()+" already exists");
    }
    
    writeData(tempResource,aggregate);
    
    
    joinTransaction().addResource(tempSuffix);
    verifyData(tempResource);

    if (logLevel.isFine())
    { log.fine("Wrote data for "+tempResource+" : tx"+txId);
    }
  }
  
  private void verifyData(Resource resource) 
    throws IOException, DataException
  {
    try
    {
      DataReader reader=new DataReader();
      reader.readFromResource
          (resource, type);
    }
    catch (SAXException x)
    { throw new DataException("Error reading back data",x);
    }

  }
  
  private void writeData(Resource resource,Aggregate<Tuple> data)
    throws DataException,IOException
  {
    DataWriter writer=new DataWriter();
    writer.writeToResource(resource, data);
  }
  
  synchronized void commit(String tempSuffix,long txId)
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
    
    if (!tempResource.exists())
    { 
      throw new IOException
          ("Transaction resource "+tempResource+" does not exist"); 
    }
    
    if (logLevel.isFine())
    {
      log.fine
        ("Renaming "+tempResource.getURI()+" to "+resource.getURI());
    }
    tempResource.renameTo(resource.getURI());
    
    if (backupURI!=null)
    { 
      Resource backupResource=Resolver.getInstance().resolve(backupURI);
      Container container=backupResource.getParent().asContainer();
      Container backupContainer=container.ensureChildContainer("history");
      if (backupContainer!=null && backupResource.exists())
      { backupResource.moveTo(backupContainer);
      }
      
    }
    
    Transaction transaction=Transaction.getContextTransaction();
    if (transaction!=null)
    { this.lastTransactionId=txId;
    }
    watcher.reset();
    if (logLevel.isFine())
    { 
      log.fine
        ("Committed resource for "+resource.getURI()+" in "+txId
        +" ("+transaction.getId()+")"
        );
    }
  }
  
  void cleanHistory(RetentionPolicy storePolicy) 
    throws DataException
  {
    getAggregate();
    if (resource==null)
    { 
      log.fine("Resource "+resourceURI+" is null");
      return;
    }
    
    try
    {
      Container history
        =resource.getParent().asContainer().ensureChildContainer("history");
      
      if (history.exists())
      { 
        Resource[] children
          =history.listChildren
            (new PatternFilter
              (Path.create(history.getURI().getPath())
              ,resource.getLocalName()+".*"
              )
            );
        Resource[] expired=storePolicy.getExclusion(children);
        if (expired.length>0)
        {
          if (logLevel.isDebug())
          {
            log.debug
              ("Deleting "+expired.length+"/"+children.length
                +" history copies for "+resource.getURI()
              );
          }
          for (Resource resource: expired)
          { resource.delete();
          }
        }
      }
      else
      { log.fine("No history exists for "+resource.getURI());
      }
    }
    catch (UnresolvableURIException x)
    { log.log(Level.SEVERE,"Error cleaning history for "+resource.getURI(),x);
    }
    catch (IOException x)
    { log.log(Level.SEVERE,"Error cleaning history for "+resource.getURI(),x);
    }
    
    
    
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
  
  
  synchronized void add(Tuple t)
  { 
    if (logLevel.isFine())
    { log.fine("Adding live "+t);
    }
    ((EditableKeyedListAggregate<Tuple>) this.aggregate).add(t);
  }
  
  synchronized void remove(Tuple t)
  { 
    if (logLevel.isFine())
    { log.fine("Removing live "+t);
    }
    ((EditableKeyedListAggregate<Tuple>) this.aggregate).remove(t);
  }
  
  synchronized void replace(Tuple ot,Tuple nt)
  { 
    if (logLevel.isFine())
    { log.fine("Replacing live "+ot+" with "+nt);
    }
    ((EditableKeyedListAggregate<Tuple>) this.aggregate).replace(ot,nt);
  }
  
  public synchronized void freeze()
  { 
    if (freezeCount>0)
    { 
      if (Transaction.getContextTransaction()!=transaction)
      { 
        try
        { 
          log.info("Waiting for lock on "+type.getURI()
            +" held by "+transaction.getId()+" in "
            +Transaction.getContextTransaction().getId()
            );
          this.wait();
        }
        catch (InterruptedException x)
        { 
          throw new RuntimeException
            ("Wait for lock on "+type.getURI()+" interrupted");
        }
      }
    }
    transaction=Transaction.getContextTransaction();
    if (transaction==null)
    { throw new IllegalStateException("Transaction required to freeze "+type.getURI());
    }

    if (logLevel.isFine())
    { log.fine("Froze ("+freezeCount+") "+type.getURI()+" in "+transaction.getId());
    }
    freezeCount++;
  }
  
  public synchronized void unfreeze()
  { 
    if (freezeCount>0)
    {
      if (Transaction.getContextTransaction()!=transaction)
      { 
        throw new RuntimeException
          ("Lock not owned by transaction "+Transaction.getContextTransaction()
          +", owner is "+transaction
          );
      }
      freezeCount--;

      if (logLevel.isFine())
      { log.fine("Unfroze ("+freezeCount+") "+resource.getURI()+" in "+transaction.getId());
      }
      if (freezeCount==0)
      {
        transaction=null;
        this.notify();
      }
    }
    else
    { throw new IllegalStateException("Not frozen: "+resource.getURI());
    }
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
    
    private ArrayList<Tuple> preparedAdds=new ArrayList<Tuple>();
    private ArrayList<JournalTuple> preparedUpdates=new ArrayList<JournalTuple>();
    
    private ArrayList<DeltaTuple> deltaList=new ArrayList<DeltaTuple>();
    
    private HashMap<Identifier,DeltaTuple> bufferMap
       =new HashMap<Identifier,DeltaTuple>();
    
    private ArrayList<ArrayJournalTuple> undoList
      =new ArrayList<ArrayJournalTuple>();
    
    private State state=State.STARTED;
    
    @SuppressWarnings("unused")
    private AbstractStore.StoreBranch storeBranch;
    
    private long txId;
    
    XmlBranch()
      throws TransactionException
    { 
      setLock.lock();
      // Don't allow reloads or updates during a transaction
      XmlQueryable.this.freeze();
      

      
      // Make sure we have latest copy from disk
      boolean ok=false;
      try
      { 
        checkInit();
        watcher.check();
        ok=true;
      }
      catch (DataException x)
      {
        throw new TransactionException
          ("Error syncing with base resource "+resource.getURI(),x);
      }
      catch (IOException x)
      { 
        throw new TransactionException
          ("Error syncing with base resource "+resource.getURI(),x);
      }
      finally
      {
        if (!ok)
        { XmlQueryable.this.unfreeze();
        }
      }
    }
    
    void addResource(String suffix)
    { resources.add(suffix);
    }

    void setStoreBranch(AbstractStore.StoreBranch storeBranch)
    {       
      this.storeBranch=storeBranch;
      this.txId=storeBranch.getTxId();     
    }

    
    /**
     * Perform implementation specific insert logic
     * 
     * @param tuple
     */
    void insert(DeltaTuple tuple)
      throws DataException
    { 

      tuple=checkBuffer(tuple);
      coalesce(tuple);
      deltaList.add(tuple);
    }
    
    /**
     * Perform implementation specific update logic
     * 
     * @param tuple
     */
    void update(DeltaTuple tuple)
      throws DataException
    {
      tuple=checkBuffer(tuple);
      coalesce(tuple);
      tuple=lockOriginal(tuple);
      deltaList.add(tuple);
    }
    

    
    /**
     * Perform implementation specific delete logic
     * 
     * @param tuple
     */
    void delete(DeltaTuple tuple)
      throws DataException
    {       
      tuple=checkBuffer(tuple);
      coalesce(tuple);
      tuple=lockOriginal(tuple);
      deltaList.add(tuple);
      
    }

    DeltaTuple checkBuffer(DeltaTuple tuple)
      throws DataException
    {
      if (tuple.isMutable())
      { 
        bufferMap.put(tuple.getId(),tuple);
        tuple=ArrayDeltaTuple.copy(tuple);
      }
      return tuple;
      
    }
    
    void notifyBuffer(Identifier id,Tuple newOriginal)
      throws TransactionException
    { 
      DeltaTuple buffer=bufferMap.get(id);
      if (buffer!=null)
      {
        try
        { buffer.updateOriginal(newOriginal);
        }
        catch (DataException x)
        { 
          throw new TransactionException("Error notifying buffer "+buffer+" of "
            +" new original "+newOriginal,x);
        }
      }
    }
    
    /**
     * Handle multiple operations applied to the same id
     * 
     * @param tuple
     */
    void coalesce(DeltaTuple tuple)
    {

    }
    
    /**
     * Lock the original while we update it
     * 
     * @param tuple
     * @throws DataException
     */
    DeltaTuple lockOriginal(DeltaTuple tuple)
      throws DataException
    {
      if (!(tuple.getOriginal() instanceof JournalTuple))
      { 
        // TODO: Rebase the original to be friendly
        throw new DataException("Not a JournalTuple "+tuple.getOriginal());
      }
      JournalTuple jt=(JournalTuple) tuple.getOriginal();
      
      
      DeltaTuple prepared=jt.prepareUpdate(tuple);  
      
      // Original might be rebased
      preparedUpdates.add((JournalTuple) prepared.getOriginal());

//      if (jt!= prepared.getOriginal() )
//      { log.fine("Added updated original "+prepared.getOriginal());
//      }
      return prepared;
    }
    
    @Override
    public void commit()
      throws TransactionException
    {
      if (state!=State.PREPARED)
      { 
        throw new TransactionException
          ("Commit can only be called when in PREPARED state, not "+state.name());
      }
      
      synchronized (XmlQueryable.this)
      {        
        for (String suffix:resources)
        { 
          
          try
          { 
            XmlQueryable.this.commit(suffix,txId);
            if (logLevel.isFine())
            { log.fine("Committed "+resource.getURI()+": "+suffix+" in "+txId);
            }
          }
          catch (IOException x)
          { throw new TransactionException("Error committing '"+suffix+"'",x);
          }
        }
        
        for (JournalTuple jt: preparedUpdates)
        { jt.commit();
        }

        state=State.COMMITTED;
      }

    }

    @Override
    public void complete()
    {
      try
      {
        if (state!=State.COMMITTED && state!=State.ABORTED)
        { rollback();
        }
      }
      catch (TransactionException x)
      { log.log(Level.WARNING,"Error rolling back incomplete transaction",x);
      }
      finally
      { 
        XmlQueryable.this.unfreeze();
        setLock.unlock();
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
      if (logLevel.isFine())
      { log.fine("Preparing "+resource.getURI()+" tx="+txId);
      }

      ArrayList<DeltaTuple> rebaseList=new ArrayList<DeltaTuple>();
      ArrayList<DeltaTuple> replaceList=new ArrayList<DeltaTuple>();
        
      for (DeltaTuple dt: deltaList)
      {
          
        try
        {
            
          Tuple orig=dt.getOriginal();
          // Handle case where the Store was reloaded during an edit
          JournalTuple storeVersion
            =(JournalTuple) XmlQueryable.this.getStoreVersion
              (orig!=null
              ?orig
              :dt
              );
          
          
          if (storeVersion!=orig)
          {
            if (storeVersion==null 
                  || (orig!=null && !(orig instanceof JournalTuple))
                  || (!storeVersion.isPreviousVersion((JournalTuple) orig)
                      && storeVersion.getTransactionId() 
                          != Transaction.getContextTransaction().getId()
                          // storeVersion is from current transaction
                     )
                 )
             
            { 
              if (logLevel.isFine())
              { 
                log.fine
                  ("Merging store changes for "+dt+" to rebase "
                  +orig+" to "+storeVersion
                  );
              }
            
              if (orig instanceof JournalTuple)
              { 
                if (preparedUpdates.remove(orig))
                { ((JournalTuple) orig).rollback();
                }
              }
              rebaseList.add(dt);
              dt=dt.rebase(storeVersion);
              replaceList.add(dt);
              
              if (storeVersion!=null)
              {  
                storeVersion.prepareUpdate(dt);
                // Original might be rebased
                preparedUpdates.add((JournalTuple) dt.getOriginal());
              }
              
            }
            else if (orig==null)
            { 
              dt=dt.updateOriginal(storeVersion);
              
              // An additional insert for a storeVersion that was placed
              // during this transaction
            }
          }
        }
        catch (DataException x)
        { throw new TransactionException("Error merging "+dt,x);
        }
        
          
        if (dt.getOriginal()!=null)
        {
          // 
          ArrayJournalTuple ot=(ArrayJournalTuple) dt.getOriginal();
          ArrayJournalTuple nt=ot.getTxVersion();
          if (nt==null)
          { 
            if (logLevel.isFine())
            { log.fine("Null txversion in "+ot);
            }
          }
          if (!dt.isDelete())
          { 
            // log.fine("Replacing "+ot+" with "+nt);
            replace(ot,nt);
            undoList.add(ot);
            
            notifyBuffer(dt.getId(),nt);
          }
          else
          { 
            remove(ot);
            undoList.add(ot);
            notifyBuffer(dt.getId(),null);
          }
        }
        else
        { 
          try
          { 
            if (logLevel.isFine())
            { log.fine("Type="+dt.getType().getURI());
            }
            ArrayJournalTuple at=ArrayJournalTuple.freezeDelta(dt);
            add(at);
            preparedAdds.add(at);
            notifyBuffer(dt.getId(),at);
            
          }
          catch (DataException x)
          { throw new TransactionException("Error adding "+dt,x);
          }            
        }
      }
        
      for (DeltaTuple dt: rebaseList)
      { deltaList.remove(dt);           
      }
      for (DeltaTuple dt: replaceList)
      { deltaList.add(dt);           
      }
        
      try
      { 
        flushResource();
      }
      catch (DataException x)
      { throw new TransactionException("Error flushing resource .tx"+txId,x);
      }
      catch (IOException x)
      { throw new TransactionException("Error flushing resource .tx"+txId,x);
      }

      if (logLevel.isFine())
      { log.fine("Prepared "+resource.getURI()+": "+txId);
      }
      
      
      this.state=State.PREPARED;
    }

    @Override
    public void rollback()
      throws TransactionException
    { 

      
      if (state!=State.COMMITTED && state!=State.ABORTED)
      { 
        Collections.reverse(deltaList);
        for (DeltaTuple dt: deltaList)
        {
          if (dt.getOriginal()!=null)
          {
            ArrayJournalTuple ot=(ArrayJournalTuple) dt.getOriginal();
            if (!dt.isDelete())
            { 
              notifyBuffer(dt.getId(),ot);
            }
            else
            { 
              notifyBuffer(dt.getId(),ot);
            }
          }
        }
        Collections.reverse(undoList);
        for (ArrayJournalTuple ot:undoList)
        { 
          ArrayJournalTuple nt=ot.getTxVersion();
          if (nt==null || nt.isDeletedVersion())
          { add(ot);
          }
          else
          { replace(nt,ot);
          }
        }
        
        Collections.reverse(preparedUpdates);
        for (JournalTuple jt: preparedUpdates)
        { jt.rollback();
        }
        
        Collections.reverse(preparedAdds);
        for (Tuple t: preparedAdds)
        { remove(t);
        }
        
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
      else
      { log.warning("Rollback on txid "+txId+" called in inapplicable state "+state);
      }

      this.state=Transaction.State.ABORTED;
    }
    
    @Override
    public String toString()
    { return super.toString()+": "
        +XmlQueryable.this.type.getURI()+" txid="+txId;
    }
  }



}
