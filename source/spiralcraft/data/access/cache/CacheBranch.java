package spiralcraft.data.access.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Identifier;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.transaction.Branch;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.Transaction.State;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

public class CacheBranch
  implements Branch
{
  private static final ClassLog log=ClassLog.getInstance(CacheBranch.class);
  final EntityCache cache;

  private final Transaction tx;
  private State state=State.STARTED;
  private final CacheResourceManager resourceManager;

  private final LinkedList<DeltaTuple> redoLog
    =new LinkedList<DeltaTuple>();
  private final HashMap<Identifier,TupleBranch> enlisted
    =new HashMap<Identifier,TupleBranch>();
  private final HashMap<Projection<?>,IndexBranch> indices
    =new HashMap<Projection<?>,IndexBranch>();

  private Level logLevel=Level.INFO;
  
  public CacheBranch(Transaction tx,EntityCache cache,CacheResourceManager resourceManager)
  { 
    this.tx=tx;
    this.cache=cache;
    this.resourceManager=resourceManager;
  }

  /**
   * <p>Return the set of Tuples which correspond to a Key Value, incorporating
   *   all uncommitted updates within the active transaction.
   * </p> 
   * 
   * <p>Called from CacheIndex.fetch() when there is an active transaction
   * </p>
   * 
   * @param key
   * @param tuple
   * @param backing
   * @return
   * @throws DataException
   */
  SerialCursor<JournalTuple> 
    fetch(Projection<Tuple> key,KeyTuple tuple,KeyedDataProvider backing)
    throws DataException
  { return getIndex(key).fetch(tuple,backing);
  }
  
  
  
  /**
   * <p>Return the transactional version of the tuple that represents the 
   *   non-normalized input retrieved from the database in the context of 
   *   an active transaction (the input may not be a globally visible
   *   version).
   * </p>
   * 
   * <p>Called from IndexBranch.fetch() when a Tuple is read from the database
   *   within an active transaction. 
   * </p>
   *   
   * @param extData
   * @return
   * @throws DataException
   */
  JournalTuple cache(Tuple extData)
    throws DataException
  { 
    TupleBranch tb=enlisted.get(extData.getId());
    if (tb==null)
    { return cache.primary.cache(extData);
    }
    else
    { return tb.redoVersion;
    }
  }
    
  /**
   * <p>Update the transactional cache copy after writing the data through
   *   to the database
   * </p>
   * 
   * <p>Called from EntityCache.update() within an active Transaction
   * </p>
   * 
   * @param delta
   * @throws DataException
   */
  JournalTuple update(DeltaTuple delta)
    throws DataException
  {
    redoLog.add(delta);
    if (delta.getOriginal()==null)
    { return logInsert(delta);
    }
    else if (delta.isDelete())
    { return logDelete(delta);
    }
    else
    { return logUpdate(delta);
    }
    
  }

  private IndexBranch getIndex(Projection<Tuple> key)
    throws DataException
  { 
    IndexBranch ret=indices.get(key);
    if (ret==null)
    { 
      ret
        =new IndexBranch
          (this,key);
      indices.put(key,ret);
    }
    return ret;
  }
  
  private JournalTuple logInsert(DeltaTuple delta)
    throws DataException
  {
    if (logLevel.isDebug())
    { log.fine("Logging insert "+delta);
    }
    JournalTuple newData=delta.freeze();
    TupleBranch tb=enlist(newData.getId());
    if (tb.delta==null && tb.storeVersion!=null)
    { 
      throw new DataException
        ("INSERT conflict: existing="+tb.storeVersion+" new:"+newData);
    }
    if (tb.redoVersion!=null)
    {
      throw new DataException
        ("INSERT conflict: existing="+tb.redoVersion+" new:"+newData);
    }
    tb.delta=delta;
    tb.redoVersion=newData;
    for (IndexBranch index:indices.values())
    { index.inserted(newData);
    }  
    return newData;
  }
  
  private JournalTuple logUpdate(DeltaTuple delta)
    throws DataException
  {
    if (logLevel.isDebug())
    { log.fine("Logging update "+delta);
    }
    Identifier id=delta.getOriginal().getId();
    TupleBranch tb=enlist(id);
    
    JournalTuple currentVersion
      =(tb.delta==null?tb.storeVersion:tb.redoVersion);
    if (currentVersion==null)
    { 
      throw new DataException
        ("UPDATE conflict: Original does not exist. id="+id+" : "+delta);
    }
    if (tb.delta==null)
    { tb.undoVersion=tb.storeVersion;
    }
    
    DeltaTuple actualDelta=currentVersion.prepareUpdate(delta);
    
    tb.redoVersion=currentVersion.getTxVersion();
    tb.delta=actualDelta;
    for (IndexBranch index:indices.values())
    { index.updated(currentVersion,tb.redoVersion);
    }
    return tb.redoVersion;
  }
  
  private JournalTuple logDelete(DeltaTuple delta)
    throws DataException
  {
    if (logLevel.isDebug())
    { log.fine("Logging delete "+delta);
    }
    if (delta.getOriginal()==null)
    { 
      log.warning("Ignoring deleted delta with null original "+delta);
      return null;
    }
    Identifier id=delta.getOriginal().getId();
    TupleBranch tb=enlist(id);
    
    JournalTuple currentVersion=
      (tb.delta==null?tb.storeVersion:tb.redoVersion);
    if (currentVersion==null)
    {
      throw new DataException
        ("DELETE conflict: Original does not exist. id="+id+" : "+delta);
    }
    if (tb.delta==null)
    { tb.undoVersion=tb.storeVersion;
    }
    
    DeltaTuple actualDelta=currentVersion.prepareUpdate(delta);
    
    tb.redoVersion=currentVersion.getTxVersion();
    tb.delta=actualDelta;
    for (IndexBranch index:indices.values())
    { index.deleted(currentVersion);
    }    
    return tb.redoVersion;
    
  }
   
  
  private TupleBranch enlist(Identifier id)
  {
    synchronized (enlisted)
    {
      TupleBranch tb=enlisted.get(id);
      if (tb==null)
      { 
        tb=new TupleBranch();
        tb.storeVersion=cache.primary.get(id);
        enlisted.put(id,tb);
      } 
      return tb;
    }
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
  public void rollback()
    throws TransactionException
  { 
    if (state==State.COMMITTED && state==State.COMPLETED)
    { throw new TransactionException("Illegal state for ROLLBACK: "+state);
    }
    
    synchronized (cache.monitor)
    {
      for (Map.Entry<Identifier,TupleBranch> entry:enlisted.entrySet())
      { 
        
        TupleBranch tb=entry.getValue();
        if (tb.applied)
        {
          cache.primary.replace(entry.getKey(),tb.undoVersion);
          for (CacheIndex index:cache.indices.values())
          { 
            if (tb.undoVersion==null)
            { index.deleted(tb.redoVersion);
            }
            else if (tb.redoVersion.isDeletedVersion())
            { index.inserted(tb.undoVersion);
            }
            else
            { index.updated(tb.redoVersion,tb.undoVersion);
            }
          }
          tb.applied=false;
        }
        if (tb.undoVersion!=null)
        { tb.undoVersion.rollback();
        }
      }
    }
    state=State.ABORTED;
  }

  @Override
  public void prepare()
    throws TransactionException
  { 
    if (state!=State.STARTED)
    { throw new TransactionException("Illegal state on PREPARE: "+state);
    }
    synchronized (cache.monitor)
    {
      for (Map.Entry<Identifier,TupleBranch> entry:enlisted.entrySet())
      { 
        TupleBranch tb=entry.getValue();
        cache.primary.replace(entry.getKey(),tb.redoVersion);
        for (CacheIndex index:cache.indices.values())
        { 
          if (tb.undoVersion==null)
          { index.inserted(tb.redoVersion);
          }
          else if (tb.redoVersion==null || tb.redoVersion.isDeletedVersion())
          { index.deleted(tb.undoVersion);
          }
          else
          { index.updated(tb.undoVersion,tb.redoVersion);
          }
        }
        tb.applied=true;
      }
      
    }
    state=State.PREPARED;
  }

  @Override
  public void commit()
    throws TransactionException
  { 
    if (state==State.COMMITTED || state==State.COMPLETED)
    { throw new TransactionException("Illegal state on COMMIT: "+state);
    }
    
    synchronized (cache.monitor)
    {
      for (Map.Entry<Identifier,TupleBranch> entry:enlisted.entrySet())
      { 
        TupleBranch tb=entry.getValue();
        if (tb.undoVersion!=null)
        { tb.undoVersion.commit();
        }
      }
    }
    state=State.COMMITTED;
  }

  @Override
  public void complete()
    throws TransactionException
  {
    if (state==State.STARTED || state==State.PREPARED)
    { throw new TransactionException("Illegal state on COMPLETE: "+state);
    }
    state=State.COMPLETED;
    resourceManager.completed(tx);
    enlisted.clear();
    indices.clear();
    redoLog.clear();
  }


}

class TupleBranch
{
  DeltaTuple delta;
  
  // The original public version on which this transaction is based
  JournalTuple storeVersion;
  
  // The version that will be the end result of this transaction
  JournalTuple redoVersion;
  
  // The first version in the chain that is locked for update or delete
  JournalTuple undoVersion;
  boolean applied;
}




