package spiralcraft.data.access.cache;

import java.util.HashMap;

import spiralcraft.data.transaction.ResourceManager;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.TransactionException;

public class CacheResourceManager
  extends ResourceManager<CacheBranch>
{

  private final EntityCache cache;
  private HashMap<Transaction,CacheBranch> branchMap
    =new HashMap<Transaction,CacheBranch>();
  
  public CacheResourceManager(EntityCache cache)
  { this.cache=cache;
  }
  
  @Override
  public CacheBranch createBranch(
    Transaction transaction)
    throws TransactionException
  { 
    CacheBranch branch=new CacheBranch(transaction,cache,this);
    synchronized (branchMap)
    { branchMap.put(transaction,branch);
    }
    return branch;
  }

  void completed(Transaction tx)
  { 
    synchronized (branchMap)
    { 
      CacheBranch branch=branchMap.remove(tx);
      if (branch==null)
      { 
        throw new IllegalStateException
          ("Transaction already completed "+tx.getId());
      }
    }
  }
  
  CacheBranch getBranch()
  {
    Transaction tx=Transaction.getContextTransaction();
    if (tx==null)
    { return null;
    }

    synchronized (branchMap)
    { return branchMap.get(tx);
    }
  }
}
