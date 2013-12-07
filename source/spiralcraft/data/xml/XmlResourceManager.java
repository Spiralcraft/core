package spiralcraft.data.xml;

import java.util.HashMap;

import spiralcraft.data.transaction.ResourceManager;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.TransactionException;

class XmlResourceManager
  extends ResourceManager<XmlBranch>
{
  private final XmlQueryable queryable;
  private final HashMap<Transaction,XmlBranch> branchMap
    =new HashMap<Transaction,XmlBranch>();  
  
  public XmlResourceManager(XmlQueryable queryable)
  { this.queryable=queryable;
  }
  
  @Override
  public XmlBranch createBranch(Transaction transaction)
    throws TransactionException
  { 
    XmlBranch branch=new XmlBranch(queryable);
    synchronized (branchMap)
    { branchMap.put(transaction,branch);
    }
    return branch;    
  }

  void completed(Transaction tx)
  { 
    synchronized (branchMap)
    { 
      XmlBranch branch=branchMap.remove(tx);
      if (branch==null)
      { 
        throw new IllegalStateException
          ("Transaction already completed "+tx.getId());
      }
    }
  }
  
  XmlBranch getBranch()
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