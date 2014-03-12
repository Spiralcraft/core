package spiralcraft.data.spi;

import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Identifier;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.log.Level;

public class ArrayJournalTuple
  extends ArrayTuple
  implements JournalTuple
{
  
  private static final boolean traceTransactionContext
    ="true".equals
      (System.getProperty
        ("spiralcraft.data.spi.ArrayJournalTuple.traceTransactionContext"
        )
      );
  
  public static ArrayJournalTuple freezeDelta(DeltaTuple delta)
    throws DataException
  { 
    ArrayJournalTuple ret=new ArrayJournalTuple(delta);
    Identifier id=delta.getId();
    if (id!=null && id.isPublic())
    { ret.setId(id);
    }
    return ret;
  }

//  private final Type<?> type;
  private final long version;
  private final long transactionId;
  private volatile JournalTuple nextVersion;
  
  // Set when a transaction is committed
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
  
  protected ArrayJournalTuple(DeltaTuple delta)
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
  public boolean isPreviousVersion(JournalTuple tuple)
  {
    if (tuple==this)
    { return true;
    }
    else if (nextVersion!=null)
    { return nextVersion.isPreviousVersion(tuple);
    }
    else 
    {
      synchronized (this)
      {
        if (transactionContext!=null)
        {
          synchronized (transactionContext)
          {
            if (this.delta==null 
                && transactionContext.transactionId==transactionId()
                )
            { return transactionContext.nextVersion.isPreviousVersion(tuple);
            }
          }
        }
      }
    }
    return false;
  }
  
  @Override
  public DeltaTuple prepareUpdate(
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
                // This transaction hasn't completed yet but this tuple has
                //   already been modified
                
                if (transactionContext.transactionId==transactionId())
                { 
                  // Multiple updates in the same transaction
                  if (!transactionContext.forward)
                  { 
                    delta=delta.updateOriginal(transactionContext.nextVersion);
                    // log.fine("Rebased "+delta);
                    transactionContext.forward=true;
                  }
                  
                  if (transactionContext.nextVersion!=null)
                  { return transactionContext.nextVersion.prepareUpdate(delta);
                  }
                  else
                  { 
                    // The last edit was a delete
                    if (!delta.isDelete())
                    { 
                      // Right now we only support stacking deletes
                      throw new DataException
                        ("Can't apply update- already deleted: "+id);
                    }
                  }
                }
                else
                {
                  // There's another transaction trying to modify this tuple
                  
                  try
                  { 
                    log.log(Level.FINE,"Transaction "+transactionId()
                      +": Waiting on transaction "
                      +transactionContext.transactionId+" created by thread "
                      +transactionContext.threadName+" : "
                      +transactionContext.delta,transactionContext.trace);
                  
                    // TODO: Soft-code timeout
                    transactionContext.wait(60000);
                  }
                  catch (InterruptedException x)
                  { throw new DataException("Interrupted waiting for transaction");
                  }
                }
              }
            }
          }
          
          if (this.delta==null)
          {
            ArrayJournalTuple nextVersion=null;
            if (delta.getOriginal()!=this)
            { delta=delta.rebase(this);
            }
            if (!delta.isDelete())
            { nextVersion=(ArrayJournalTuple) delta.freeze();
            }
            transactionContext
              =new TransactionContext(nextVersion,delta);
            return delta;
          }
        }
      }
    }
    
    JournalTuple latestVersion
      =latestVersion();
    if (latestVersion!=null)
    { return latestVersion().prepareUpdate(delta);
    }
    else
    { return delta;
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
          if (transactionContext.forward)
          { 
            log.fine("Rolling back forward transaction "+transactionContext.nextVersion);
            transactionContext.nextVersion.rollback();
          }
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
          if (transactionContext.forward)
          { 
            log.fine("Committing forward transaction "+transactionContext.nextVersion);
            transactionContext.nextVersion.commit();
          }
          // Let waiters advance to the next version
          // TODO: make this a fifo queue
          transactionContext.notifyAll();
          transactionContext=null;
        }
      }
      else
      { log.warning("Nothing to commit in "+this+"  "+(nextVersion!=null?"\r\nnextVersion = "+nextVersion:""));
      }
    }
  }
  
  @Override
  public DeltaTuple update(DeltaTuple delta)
    throws DataException
  {
    DeltaTuple next=prepareUpdate(delta);
    commit();
    return next;
    
  }

  @Override
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
  public AbstractTuple copyTupleField(Tuple tuple)
    throws DataException
  { 
    if (tuple instanceof DeltaTuple)
    { return ArrayTuple.freezeDelta((DeltaTuple) tuple);
    }
    else
    { return new ArrayTuple(tuple);
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
  
  @Override
  public String toString()
  { return super.toString()+" version="+version+", transaction="+transactionId
     +(nextVersion!=null
       ?"(nextVersion="+nextVersion.getVersion()+":"+nextVersion.getTransactionId()+")"
       :" (latest version)"
       )
     +(transactionContext!=null?" LOCKED BY tx"+transactionContext.transactionId:"")
     ;
  }
  
  class TransactionContext
  {
    public final ArrayJournalTuple nextVersion;
    public final DeltaTuple delta;
    public final Exception trace;
    public final String threadName;
    public final long transactionId;
    public boolean forward=false;

    public TransactionContext
      (ArrayJournalTuple nextVersion
      ,DeltaTuple delta
      )
    { 
      this.nextVersion=nextVersion;
      this.delta=delta;
      if (traceTransactionContext)
      { this.trace=new Exception();
      }
      else
      { this.trace=null;
      }
      this.threadName=Thread.currentThread().getName();
      this.transactionId=transactionId();
    }
  }


}
