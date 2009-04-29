//
// Copyright (c) 1998,2009 Michael Toth
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
import java.util.concurrent.atomic.AtomicInteger;

import spiralcraft.log.ClassLog;

/**
 * <p>Represents a Transaction- a set of data modifications that comprise 
 *   an atomic unit of work. A Transaction supports A.C.I.D. properties-
 *   Atomicity, Consistency, Isolation and Durability.
 * </p>
 * 
 * <p>A Transaction consists of a number of Branches and optionally nested 
 *   Transactions. Each branch is responsible for managing a given data
 *   resource (eg. database connection, uncommitted buffers, etc) that
 *   participates in the transaction. 
 */
public class Transaction
{
  private static final ClassLog log
    =ClassLog.getInstance(Transaction.class);

  
  private static ThreadLocal<Transaction> TRANSACTION_LOCAL
    =new ThreadLocal<Transaction>();
  
  private static final AtomicInteger nextId=new AtomicInteger();
  
  public enum Nesting
  { PROPOGATE,ISOLATE;
  }
  
  public enum Requirement
  { REQUIRED,OPTIONAL,NONE;
  }
  
  public enum State
  { STARTED,PREPARED,COMMITTED,ABORTED,COMPLETED
  }

  /**
   * <P>Start a new Transaction for the current Thread context, with the specified
   *   nesting relationship in the existing Transaction, if any.
   *   
   * <P>The caller of this method MUST call Transaction.complete()
   */
  public static final Transaction startContextTransaction(Nesting nesting)
  { 
    Transaction transaction=TRANSACTION_LOCAL.get();
    if (nesting==Nesting.PROPOGATE)
    {
      if (transaction!=null)
      { return transaction.subTransaction();
      }
      else
      { return new Transaction(null,Nesting.ISOLATE);
      }
    }
    else
    { return new Transaction(null,nesting);
    }
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
  
  private final int id=nextId.getAndIncrement();
  
  private final Transaction parent;
  // private Transaction sub;
//  private Nesting nesting;
  private State state;
  private boolean rollbackOnly;
  private ArrayList<Transaction> subtransactions
    =new ArrayList<Transaction>();
  
  private boolean debug;
  
  
  /**
   * Create a new Transaction nested in the given Transaction 
   *
   * @param parent
   * @param nesting
   */
  Transaction(Transaction parent,Nesting nesting)
  { 
    this.parent=parent;
//    this.nesting=nesting;
    TRANSACTION_LOCAL.set(this);
    this.state=State.STARTED;
    if (debug)
    { log.fine("Started transaction #"+id);
    }
  }
  
  //void addSubTransaction(Transaction sub)
  //{ 
  //}
  
  public int getId()
  { return id;
  }
  
  public Transaction subTransaction()
  {
    Transaction child=new Transaction(this,Nesting.PROPOGATE);
    child.setDebug(debug);
    subtransactions.add(child);
    if (debug)
    { log.fine("Added subtransaction #"+child.getId()+" to transaction #"+id);
    }
    return child;
  }
  
  public void setDebug(boolean debug)
  { 
    this.debug=debug;
    if (debug)
    { 
      log.fine
        ("Starting debug of transaction #"+getId()
         +", child of #"+(parent!=null?parent.getId():"none")
         +" in state "+getState()
        );
    }
  }
  
  /**
   * Specify that the Transaction can only rollback on completion
   */
  public void rollbackOnComplete()
  { 
    if (debug)
    {
      log.fine
        ("rollbackOnly for Transaction #"+getId());
    }
    rollbackOnly=true;
    if (parent!=null)
    { parent.rollbackOnComplete();
    }
  }

  synchronized Branch branch(ResourceManager<?> manager)
    throws TransactionException
  { 
    Branch branch=branchMap.get(manager);
    if (branch==null)
    { 
      if (parent!=null)
      {
        // Propogated transaction uses same branches. Resources use different
        //   ResourceManagers, so they can still manage their own commits.
        branch=parent.branch(manager);
      }
      else
      {
        if (debug)
        {
          log.fine
            ("New branch for Transaction #"+getId()+": "+manager.toString());
        }

        branch=manager.createBranch(this);
        branchMap.put(manager,branch);

        if (branch.is2PC())
        { branches.add(branch);
        }
        else if (llrBranch==null)
        { 
          if (debug)
          {
            log.fine
              ("New branch for Transaction #"+getId()+" is LLR");
          }
          llrBranch=branch;
        }
        else
        { throw new TransactionException
            ("Transaction already contains an non-2PC branch");
        }
      }
    }

    return branch;
  }
  

  
  public synchronized void commit()
    throws TransactionException
  {
    // Note that a subTransaction has no branches- the following code
    //   will have no effect?
    
    // XXX Exception handling logic is critical here
    
    if (parent!=null)
    { 
      if (debug)
      {
        log.fine
          ("Commit Transaction #"+getId()
          +" child of #"+parent.getId()
          +" has no effect"
          );
      }
      
      // Parent will call prepare() and commitLocal()
    }
    else if (rollbackOnly)
    { throw new RollbackException();
    }
    else
    {
      if (state==State.STARTED)
      { 
        if (debug)
        {
          log.fine
            ("Root transaction #"+getId()+" running missed prepare");
        }
        prepare();
      }
      commitLocal();
    }
    
  }
  
  public State getState()
  { return state;
  }
  
  public synchronized void commitLocal()
    throws TransactionException
  {
  
    if (state==State.PREPARED)
    {
      if (debug)
      {
        log.fine
          ("Transaction #"+getId()+", child of "
          +(parent!=null?parent.getId():"none")
          +" committing"
          );
      }

      if (llrBranch!=null)
      { 
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()+" committing LLR branch");
        }


        llrBranch.commit();
   
        if (llrBranch.getState()!=State.COMMITTED)
        {
          rollback();
          throw new TransactionException
            ("Transaction Aborted: LLR branch "+llrBranch+" failed to commit");
        }
      }


    
      for (Transaction transaction : subtransactions)
      { 
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()
            +" committing subtransaction #"+transaction.getId()
            );
        }
        transaction.commitLocal();
        if (transaction.getState()!=State.COMMITTED)
        { 
          rollback();
          throw new TransactionException
          ("Transaction Aborted after Partial Commit: subtransaction "
            +transaction+" failed to commit"
          );
        }
      }

      for (Branch branch: branches)
      { 
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()+" committing branch "+branch);
        }
        branch.commit();
      }
      state=State.COMMITTED;
    }
    else
    { 
      throw new TransactionException
        ("Transaction not PREPARED: state="+getState());
    }
    
  }
  
  public synchronized void prepare()
    throws TransactionException
  {
    // new Exception().printStackTrace();
    if (state==State.STARTED)
    {
      if (debug)
      {
        log.fine
          ("Transaction #"+getId()+" preparing");
      }

      for (Transaction transaction : subtransactions)
      { 
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()
            +" preparing subtransaction #"+transaction.getId()
            );
        }
        
        transaction.prepare();
        if (transaction.getState()!=State.PREPARED)
        { 
          rollback();
          throw new TransactionException
            ("Transaction Aborted: subtransaction "+transaction
            +" failed to prepare"
            );
        }
      }

      for (Branch branch: branches)
      { 
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()
            +" preparing branch "+branch.toString()
            );
        }
        branch.prepare();
        if (branch.getState()!=State.PREPARED)
        { 
          rollback();
          throw new TransactionException
            ("Transaction Aborted: branch "+branch+" failed to prepare");
        }
      }
      state=State.PREPARED;
    }
    else
    {
      throw new TransactionException
        ("Transaction not STARTED: state="+getState());
    }

    
  }
  
  public synchronized void rollback()
    throws TransactionException
  {
    
    if (state==State.STARTED
        || state==State.PREPARED
        )
    {
      
      if (debug)
      {
        log.fine
          ("Transaction #"+getId()
          +" rolling back "
          );
      }
        
      for (Branch branch: branches)
      { 
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()
            +" rolling back "+branch.toString()
            );
        }
        
        try
        { branch.rollback();
        }
        catch (TransactionException x)
        { 
          // XXX Temporary
          x.printStackTrace();
        }
      }

      for (Transaction transaction : subtransactions)
      { 
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()
            +" rolling back subtransaction #"+transaction.getId()
            );
        }
        transaction.rollback();
      }
      
      if (llrBranch!=null)
      { 
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()
            +" rolling back LLR branch "+llrBranch
            );
        }
        llrBranch.rollback();
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
    
    if (parent!=null)
    {
      if (debug)
      {
        log.fine
          ("Transaction #"+getId()
          +" completed- returning control to parent transaction #"+parent.getId()
          );
      }

      TRANSACTION_LOCAL.set(parent);
      return;
    }
    
    if (state==State.COMMITTED
        || state==State.ABORTED
        )
    { 
      if (debug)
      {
        log.fine
          ("Transaction #"+getId()
          +" in state "+getState()
          +"- returning control to parent transaction #"
          +(parent!=null?parent.getId():"none")
          );
      }
      TRANSACTION_LOCAL.set(parent);
    }
    else
    {
    
    
      // Commit or rollback here
      if (!rollbackOnly)
      { 
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()
            +" in state "+getState()
            +"- committing on complete"
            );
        }
        
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
        if (debug)
        {
          log.fine
            ("Transaction #"+getId()
            +" in state "+getState()
            +"- rolling back on complete"
            );
        }
        
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
      TRANSACTION_LOCAL.set(parent);
    }
    
    for (Branch branch:branches)
    { 
      if (debug)
      {
        log.fine
          ("Transaction #"+getId()
          +" in state "+getState()
          +"- completing branch "+branch
          );
      }
      branch.complete();
    }
    
    if (llrBranch!=null)
    { 
      if (debug)
      {
        log.fine
          ("Transaction #"+getId()
          +" in state "+getState()
          +"- completing LLR branch "+llrBranch
          );
      }
      llrBranch.complete();
    }
    state=State.COMPLETED;
    
  }
  
}
