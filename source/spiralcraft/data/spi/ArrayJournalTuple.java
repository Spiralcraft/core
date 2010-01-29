package spiralcraft.data.spi;

import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.transaction.Transaction;

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
    super(type.getFieldSet());
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
    this.version=original.getVersion()+1;
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
        if (transactionContext!=null)
        { 
          try
          { 
            log.fine("Waiting on transaction...");
            transactionContext.wait();
          }
          catch (InterruptedException x)
          { throw new DataException("Interrupted waiting for transaction");
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
    return latestVersion().update(delta);
  }

  @Override
  public void rollback()
  {
    // TODO Auto-generated method stub
    synchronized (this)
    { 
      transactionContext.notifyAll();
      transactionContext=null;
    }
    
  }

  @Override
  public void commit()
  {
    
    synchronized (this)
    { 
      nextVersion=transactionContext.nextVersion;
      delta=transactionContext.delta;
      transactionContext.notifyAll();
      transactionContext=null;
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

    public TransactionContext
      (ArrayJournalTuple nextVersion
      ,DeltaTuple delta
      )
    { 
      this.nextVersion=nextVersion;
      this.delta=delta;
    }
  }

}
