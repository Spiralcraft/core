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

import spiralcraft.data.access.EntityAccessor;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.core.ProjectionImpl;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;
import spiralcraft.data.spi.AbstractAggregateQueryable;
import spiralcraft.data.spi.ArrayJournalTuple;
import spiralcraft.data.spi.EditableKeyedListAggregate;
import spiralcraft.data.spi.KeyedListAggregate;
import spiralcraft.data.spi.ListCursor;
import spiralcraft.data.sax.DataReader;
import spiralcraft.data.sax.DataWriter;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.Transaction.Nesting;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.Aggregate;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Field;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.Key;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.time.Clock;
import spiralcraft.util.Path;
import spiralcraft.util.refpool.URIPool;
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
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.IOException;

import org.xml.sax.SAXException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.kit.ConstantChannel;
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
  implements Queryable<Tuple>,EntityAccessor<Tuple>
{
  protected final ClassLog log
    =ClassLog.getInstance(getClass());

  protected Level logLevel
    =ClassLog.getInitialDebugLevel(getClass(),null);
  
  ArrayList<Key<?>> uniqueKeys
    =new ArrayList<Key<?>>();  
  final Lock setLock=new ReentrantLock(true);
  ResourceWatcher watcher;
  Resource resource;
  ThreadLocalChannel<Tuple> localData;
  ArrayList<BoundQuery<?,Tuple>> uniqueQueries
    =new ArrayList<BoundQuery<?,Tuple>>();
  Type<?> type;
  XmlResourceManager resourceManager
    =new XmlResourceManager(this);
  KeyedListAggregate<Tuple> aggregate;
  ListCursor<Tuple> emptyCursor;      

  
  protected Focus<Queryable<Tuple>> selfFocus;
  {
    try
    { 
      selfFocus
        =new SimpleFocus<Queryable<Tuple>>
          (ConstantChannel.<Queryable<Tuple>>forBean(this));
    }
    catch (BindException x)
    { throw new RuntimeDataException("",x);
    }
    

  }
  
  private SimpleDateFormat dateFormat
    =new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
  
  private URI resourceURI;
  private URI resourceContextURI;
  
  
  
//  private KeyedListAggregate<Tuple> snapshot;
  
  private Exception exception;
  
  private boolean autoCreate;
  
  
  private int freezeCount;
  private long lastTransactionId;
  
  
  private volatile Transaction transaction;
  private Focus<Tuple> localFocus;
  private BoundQuery<?,Tuple> originalQuery;
  
  
  private XmlStore store;
  
  private Queryable<Tuple> backingQueryable
    =new AbstractAggregateQueryable<Tuple>()
  {

    @Override
    protected Aggregate<Tuple> getAggregate()
      throws DataException
    { return XmlQueryable.this.getAggregate();
    }

    @Override
    protected Type<?> getResultType()
    { return XmlQueryable.this.getResultType();
    }
  };
  
  public void setLogLevel(Level logLevel)
  { this.logLevel=logLevel;
  }
  
  public Level getLogLevel()
  { return logLevel;
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
  public boolean containsType(Type<?> type)
  { return type.isAssignableFrom(getResultType());
  }



  @Override
  public Type<?>[] getTypes()
  { return new Type<?>[] {getResultType()};
  }
  
  
  @Override
  public BoundQuery<?,Tuple> query(Query q, Focus<?> context)
    throws DataException
  { 
    if (q==null)
    { throw new IllegalArgumentException("Query cannot be null");
    }
    
    BoundQuery<?,Tuple> ret=solve(q,context);
    if (ret==null)
    { ret=q.solve(context, this);
    }
    ret.resolve();
    if (logLevel.isDebug())
    { log.debug(q.toString()+" bound to "+ret);
    }
    return ret;
  }  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    this.store=LangUtil.assertInstance(XmlStore.class,focusChain);

    localData=new ThreadLocalChannel<Tuple>
      (DataReflector.<Tuple>getInstance(getResultType()));
    localFocus=focusChain.chain(localData);
    Key<?> primaryKey=getResultType().getPrimaryKey();
    if (primaryKey!=null)
    { 
      try
      { originalQuery=backingQueryable.query(primaryKey.getQuery(), localFocus);
      }
      catch (DataException x)
      { throw new BindException("Error resolving identity query for "+getResultType().getURI(),x);
      }
    }
    else
    { 
      try
      {

        ArrayList<String> fields=new ArrayList<String>();
        for (Field<?> field: getResultType().getFieldSet().fieldIterable())
        { 
          if ( !field.isTransient() && !field.isStatic() )
          { fields.add(field.getName());
          }
        }
        String[] fieldNames=fields.toArray(new String[fields.size()]);
        
        ProjectionImpl<Tuple> equijoin
          =new ProjectionImpl<Tuple>
            (getResultType().getFieldSet()
            ,fieldNames
            );
        originalQuery=backingQueryable.query(equijoin.getIdentityQuery(), localFocus);
      }
      catch (DataException x)
      { throw new BindException("Error resolving identity query",x);
      }
      
      
    }
    
  
    return focusChain;

  }
  
  void bindDRI(Focus<?> context)
    throws BindException
  {
    if (this.store==null)
    { throw new BindException("Not bound: "+type.getURI()+"  "+toString());
    }
    try
    {
    
      for (Key<?> key: getResultType().getKeys())
      {
        if (key.isUnique() || key.isPrimary())
        { 
          // Create queries for unique keys and associate the Key 
          //   with the query via a parallel list for error reporting.
          uniqueQueries.add(store.query(key.getQuery(),localFocus));
          uniqueKeys.add(key);
        }
      }
    }
    catch (DataException x)
    { throw new BindException("Error binding DRI rules for "+type.getURI()); 
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
        emptyCursor=new ListCursor<Tuple>
          (getResultType().getScheme(),new LinkedList<Tuple>());
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
        reader.setStringPool(store!=null?store.getStringPool():null);
        
        long startTime=Clock.instance().approxTimeMillis();
        Aggregate<Tuple> orig
          =(Aggregate<Tuple>) reader.readFromResource
            (resource, type);
        long finishTime=Clock.instance().approxTimeMillis();
        if (finishTime-startTime>1000)
        {
          log.info
            ("Resource loaded "+orig.size()
            +" tuples in "+( (finishTime-startTime) /(float) 1000)
            +" seconds from "+resource.getURI()
            );
        }

        aggregate=new EditableKeyedListAggregate<Tuple>(orig.getType());
        for (Tuple t: orig)
        {
          ArrayJournalTuple nt=new ArrayJournalTuple(t);
          add(nt);
        }
        if (store!=null)
        { store.onReload(new Type<?>[] {getResultType()});
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
  
  void checkUpToDate()
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
  }
  
  protected synchronized KeyedListAggregate<Tuple> getAggregate()
    throws DataException
  { 

    checkUpToDate();
    
    return aggregate;
  }
  
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
  
  /**
   * Return a cursor over all the data that incorporates data from the 
   *   current transaction
   */
  SerialCursor<Tuple> getCursor()
    throws DataException
  {
    XmlBranch branch=resourceManager.getBranch();
    if (branch!=null)
    { return branch.getCursor();
    }
    return getPublicCursor();
  }

  /**
   * Return a cursor over all the data that incorporates data from the 
   *   current transaction
   */
  SerialCursor<Tuple> getCursor(Projection<Tuple> projection,KeyTuple key)
    throws DataException
  {
    XmlBranch branch=resourceManager.getBranch();
    if (branch!=null)
    { return branch.getCursor(projection,key);
    }
    return getPublicCursor(projection,key);
  }
  
  SerialCursor<Tuple> getPublicCursor()
    throws DataException
  {
    // TODO: To incorporate TX results, use source.getCursor();    
    Aggregate<Tuple> aggregate=getAggregate();
    if (aggregate==null)
    {
      throw new DataException
        ("Aggregate of "+getResultType().getURI()+" from "
          +getClass().getName()
          +" is null- cannot perform query"
        );
    }
    return new ListCursor<Tuple>(aggregate);
    
  }
  
  SerialCursor<Tuple> getPublicCursor(Projection<Tuple> projection,KeyTuple key)
    throws DataException
  {
    KeyedListAggregate<Tuple> aggregate=getAggregate();
    if (aggregate==null)
    { throw new DataException("Aggregate is null- cannot perform query");
    }
    Aggregate.Index<Tuple> index=aggregate.getIndex(projection, true);

    Aggregate<Tuple> result=index.get(key);
    if (result==null)
    { return emptyCursor;
    }
    else
    { return new ListCursor<Tuple>(result);
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
      (URIPool.create
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
        (URIPool.create(this.resource.getURI().toString()+tempSuffix));

    URI backupURI=null;
    int seq=0;
    if (resource.exists())
    {
      while (backupURI==null 
              || Resolver.getInstance().resolve(backupURI).exists()
            )
      {
        backupURI=URIPool.create
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
    checkUpToDate();
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
        (URIPool.create(this.resource.getURI().toString()+tempSuffix));
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
  
  @Override
  public BoundQuery<?,Tuple> getAll(Type<?> type) throws DataException
  {
    BoundScan scan=new BoundScan(new Scan(getResultType()),selfFocus,this);
    scan.resolve();
    return scan;
  }
  
  @Override
  public BoundQuery<?,Tuple> solve(Query q, Focus<?> context)
    throws DataException
  { 
    if (q==null)
    { throw new IllegalArgumentException("Query cannot be null");
    }
    
    BoundQuery<?,Tuple> ret=null;
    if (q instanceof Scan
        && q.getType().isAssignableFrom(getResultType())
        )
    { ret=new BoundScan((Scan) q,context,this);
    }
    else if ( (q instanceof EquiJoin)
        && (q.getSources().get(0) instanceof Scan)
        && q.getType().isAssignableFrom(getResultType())
        )
    { ret=new BoundIndexScan((EquiJoin) q,context,this);
    }
    return ret;
  }


}
