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
package spiralcraft.data.transaction;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * <P>Represents a Transaction- a set of data modifications that comprise an atomic unit of
 *   work. A Transaction supports A.C.I.D. properties- Atomicity, Consistency, Isolation
 *   and Durability.
 * 
 * <P>A Transaction consists of a number of Branches and optionally nested Transactions.
 *   Each branch is responsible for managing a given data resource that participates in the
 *   transaction. 
 */
public class Transaction
{
  private static ThreadLocal<Transaction> TRANSACTION_LOCAL
    =new ThreadLocal<Transaction>();
  
  public enum Nesting
  { PROPOGATE,ISOLATE;
  };
  
  public enum State
  { STARTED,PREPARED,COMMITTED,ABORTED
  };

  /**
   * <P>Start a new Transaction for the current Thread context, with the specified
   *   nesting relationship in the existing Transaction, if any.
   *   
   * <P>The caller of this method MUST call Transaction.complete()
   */
  public static final Transaction startContextTransaction(Nesting nesting)
  { 
    Transaction transaction=TRANSACTION_LOCAL.get();
    return new Transaction(transaction,nesting);
  }
  
  /**
   * <P>Obtain the Transaction associated with the current Thread context
   */
  public static final Transaction getContextTransaction()
  { return TRANSACTION_LOCAL.get();
  }
  
  private HashMap<ResourceManager<?>,Branch> branchMap
    =new HashMap<ResourceManager<?>,Branch>();
  
  private ArrayList<Branch> branches=new ArrayList<Branch>();
  private Branch llrBranch;
  
  private final Transaction nestee;
  // private Transaction sub;
  private Nesting nesting;
  private State state;
  private boolean rollbackOnly;
  
  /**
   * Create a new Transaction nested in the given Transaction 
   */
  Transaction(Transaction nestee,Nesting nesting)
  { 
    this.nestee=nestee;
    this.nesting=nesting;
    if (this.nesting==Nesting.PROPOGATE && nestee!=null)
    { 
      throw new UnsupportedOperationException("Subtransactions not yet supported");
      // nestee.setSubTransaction(this);
    }
    TRANSACTION_LOCAL.set(this);
  }
  
  //void addSubTransaction(Transaction sub)
  //{ 
  //}
  
  /**
   * Specify that the Transaction can only rollback on completion
   */
  public void rollbackOnComplete()
  { rollbackOnly=true;
  }
  

  synchronized Branch branch(ResourceManager<?> manager)
    throws TransactionException
  { 
    Branch branch=branchMap.get(manager);
    if (branch==null)
    { 
      branch=manager.createBranch(this);
      branchMap.put(manager,branch);

      //XXX: Handle LogLastResource condition- last resource is non 2PC resource
      //  which is std. single database scenario.
      
      if (branch.is2PC())
      { branches.add(branch);
      }
      else if (llrBranch==null)
      { llrBranch=branch;
      }
      else
      { throw new TransactionException("Transaction already contains an non-2PC branch");
      }
    }
    return branch;
  }
  
  public synchronized void commit()
    throws TransactionException
  {
    // XXX Exception handling logic is critical here
    
    if (state==State.STARTED
        )
    {
      for (Branch branch: branches)
      { branch.prepare();
      }
    }
    
    for (Branch branch: branches)
    { 
      if (branch.getState()!=State.PREPARED)
      { 
        rollback();
        throw new TransactionException
          ("Transaction Aborted: branch "+branch+" failed to prepare");
      }
    }

    if (llrBranch!=null)
    { 
      llrBranch.commit();
   
      if (llrBranch.getState()!=State.COMMITTED)
      {
        rollback();
        throw new TransactionException
          ("Transaction Aborted: LLR branch "+llrBranch+" failed to commit");
      }
    }
    
    for (Branch branch: branches)
    { branch.commit();
    }
    
  }
  
  public synchronized void rollback()
    throws TransactionException
  {
    if (state==State.STARTED
        || state==State.PREPARED
        )
    {
      for (Branch branch: branches)
      { 
        try
        { branch.rollback();
        }
        catch (TransactionException x)
        { 
          // XXX Temporary
          x.printStackTrace();
        }
      }
      
      if (llrBranch!=null)
      { llrBranch.rollback();
      }
    }
    else
    { throw new IllegalStateException("Cannot rollback in state "+state);
    }
  }
  
  public synchronized void complete()
  {
    // Complete sub-transactions
    // if (sub!=null)
    //{ sub.complete();
    //}

    if (TRANSACTION_LOCAL.get()!=this)
    { 
      throw new IllegalStateException
        ("Transaction.complete() must be called from creating thread");
    }
    
    if (state==State.COMMITTED
        || state==State.ABORTED
        )
    { 
      TRANSACTION_LOCAL.set(nestee);
    }
    else
    {
    
    
      // Commit or rollback here
      if (!rollbackOnly)
      { 
        try
        { commit();
        }
        catch (TransactionException x)
        { 
          // XXX Temporary
          x.printStackTrace();
        }
      }
      else
      { 
        try
        { rollback();
        }
        catch (TransactionException x)
        { 
          // XXX Temporary
          x.printStackTrace();
        }
      }
    
    
      // Pop the stack
      TRANSACTION_LOCAL.set(nestee);
    }
    
    for (Branch branch:branches)
    { branch.complete();
    }
    
    if (llrBranch!=null)
    { llrBranch.complete();
    }
    
  }
  
}
