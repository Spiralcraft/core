package spiralcraft.data.spi;

import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.log.Level;

public class ArrayJournalTuple
  extends ArrayTuple
  implements JournalTuple
{

//  private final Type<?> type;
  private final long version;
  private final long transactionId;
  private volatile JournalTuple nextVersion;
  private volatile DeltaTuple delta;
  private volatile TransactionContext transactionContext;
   
  
  public ArrayJournalTuple(Type<?> type)
  { 
    super(type.getScheme());
//    this.type=type;
    
    this.version=0;
    transactionId=transactionId();    
  }
  
  public ArrayJournalTuple(Tuple original)
    throws DataException
  { 
    super(original);
//    this.type=original.getType();
    
    this.version=0;
    transactionId=transactionId();
  }
  
  public ArrayJournalTuple(DeltaTuple delta)
    throws DataException
  { 
    super(delta);
//    this.type=delta.getType().getArchetype();
    JournalTuple original=(JournalTuple) delta.getOriginal();
    this.version=original!=null?original.getVersion()+1:0;
    transactionId=transactionId();
    
  }
  

  
  private long transactionId()
  {
    Transaction transaction=Transaction.getContextTransaction();
    if (transaction!=null)
    { return transaction.getId();
    }
    else
    { return 0;
    }

  }
  


  @Override
  public boolean isDeletedVersion()
  {
    return delta!=null && delta.isDelete();
  }

  @Override
  public JournalTuple latestVersion()
  { 
    if (nextVersion!=null)
    { return nextVersion.latestVersion();
    }
    else
    { 
      if (delta!=null && delta.isDelete())
      { return null;
      }
      else
      { return this;
      }
    }
  }

  @Override
  public DeltaTuple nextDelta()
  { return delta;
  }

  @Override
  public JournalTuple nextVersion()
  { return nextVersion;
  }

  @Override
  public JournalTuple prepareUpdate(
    DeltaTuple delta)
    throws DataException
  {
    if (this.delta==null)
    { 
      synchronized (this)
      {
        if (this.delta==null)
        {
          if (transactionContext!=null)
          { 
            synchronized (transactionContext)
            {
              if (this.delta==null)
              {
                try
                { 
                  log.log(Level.FINE,"Waiting on transaction created by thread "
                    +transactionContext.threadName+" : "
                    +transactionContext.delta,transactionContext.trace);
                
                  transactionContext.wait();
                }
                catch (InterruptedException x)
                { throw new DataException("Interrupted waiting for transaction");
                }
              }
            }
          }
          
          if (this.delta==null)
          {
            ArrayJournalTuple nextVersion=null;
            if (!delta.isDelete())
            { nextVersion=new ArrayJournalTuple(delta);
            }
            transactionContext
              =new TransactionContext(nextVersion,delta);
            return nextVersion;
          }
        }
      }
    }
    
    JournalTuple latestVersion
      =latestVersion();
    if (latestVersion!=null)
    { return latestVersion().update(delta);
    }
    else
    { return null;
    }
    
  }

  @Override
  public void rollback()
  {
    synchronized (this)
    { 
      if (transactionContext!=null)
      {
      
        synchronized (transactionContext)
        {
          // log.fine("Rolling back "+transactionContext.delta);
          
          // Let waiters contend for the next version
          // If we don't notify all, nothing else will be around to
          //   notify remaining waiters.
          transactionContext.notifyAll();
          transactionContext=null;
        }
      }
      else
      { log.warning("Nothing to rollback in "+this);
      }
    }
    
  }

  @Override
  public void commit()
  {
    
    synchronized (this)
    { 
      if (transactionContext!=null)
      {
        synchronized (transactionContext)
        {
          nextVersion=transactionContext.nextVersion;
          delta=transactionContext.delta;
          // Let waiters advance to the next version
          // TODO: make this a fifo queue
          transactionContext.notifyAll();
          transactionContext=null;
        }
      }
      else
      { log.warning("Nothing to commit in "+this);
      }
    }
  }
  
  @Override
  public JournalTuple update(DeltaTuple delta)
    throws DataException
  {
    JournalTuple next=prepareUpdate(delta);
    commit();
    return next;
    
  }

  public ArrayJournalTuple getTxVersion()
  {
    synchronized (this)
    {
      if (transactionContext!=null)
      { return transactionContext.nextVersion;
      }
      else
      { return null;
      }
    }

    
  }

  @Override
  public long getTransactionId()
  { return transactionId;
  }

  @Override
  public long getVersion()
  { return version;
  }
  
  class TransactionContext
  {
    public final ArrayJournalTuple nextVersion;
    public final DeltaTuple delta;
    public final Exception trace;
    public final String threadName;

    public TransactionContext
      (ArrayJournalTuple nextVersion
      ,DeltaTuple delta
      )
    { 
      this.nextVersion=nextVersion;
      this.delta=delta;
      this.trace=new Exception();
      this.threadName=Thread.currentThread().getName();
    }
  }


}
