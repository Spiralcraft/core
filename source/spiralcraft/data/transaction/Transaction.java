//
// Copyright (c) 1998,2010 Michael Toth
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
import java.util.concurrent.atomic.AtomicLong;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.RollingIterable;

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
  
  private static final AtomicLong nextId
    =new AtomicLong(System.currentTimeMillis());
  
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
    { return new Transaction(transaction,nesting);
    }
  }
  
  /**
   * <P>Obtain the Transaction associated with the current Thread context
   */
  public static final Transaction getContextTransaction()
  { return TRANSACTION_LOCAL.get();
  }
  
  private final HashMap<ResourceManager<?>,Branch> branchMap
    =new HashMap<ResourceManager<?>,Branch>();
  
  private final RollingIterable<Branch> branches
    =new RollingIterable<Branch>();
  
  private Branch llrBranch;
  
  private final long id=nextId.getAndIncrement();
  
  private final Transaction parent;
  private Nesting nesting;
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
    this.nesting=nesting;
    TRANSACTION_LOCAL.set(this);
    this.state=State.STARTED;
    if (debug)
    { log.fine("Started transaction #"+id);
    }
  }
  
  //void addSubTransaction(Transaction sub)
  //{ 
  //}
  
  public long getId()
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
         +", "+nesting+" child of #"+(parent!=null?parent.getId():"none")
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
    if (nesting==Nesting.PROPOGATE && parent!=null)
    { parent.rollbackOnComplete();
    }
  }

  synchronized Branch branch(ResourceManager<?> manager)
    throws TransactionException
  { 
    try
    {
      Branch branch=branchMap.get(manager);
      if (branch==null)
      { 
        if (parent!=null && nesting==Nesting.PROPOGATE)
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
    catch (RuntimeException x)
    { 
      rollbackOnComplete();
      throw new TransactionException
        ("xid "+id+": Runtime exception creating branch for resource manager "
        +manager
        ,x);
    }
  }
  

  @Override
  public String toString()
  { 
    
    StringBuffer buffer=new StringBuffer();
    if (branches!=null)
    {
      for (Branch branch:branches)
      { buffer.append(branch.toString()+" , ");
      }
    }
    return super.toString()+" txid="+id+" "
      +buffer.toString();
  }
  
  public synchronized void commit()
    throws TransactionException
  {

      
    // Note that a subTransaction has no branches- the following code
    //   will have no effect?

    // XXX Exception handling logic is critical here

    if (parent!=null && nesting==Nesting.PROPOGATE)
    { 
      if (debug)
      {
        log.fine
        ("Commit Transaction #"+getId()
          +" propogated child of #"+parent.getId()
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
  
    try
    {
      if (state==State.PREPARED)
      {
        if (debug)
        {
          log.fine
          ("Transaction #"+getId()+", "+nesting+" child of "
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
    catch (RuntimeException x)
    { 
      throw new TransactionException
        ("xid "+id
        +": Runtime exception committing transaction- POTENTIAL PARTIAL COMMIT"
        +" - RESOURCE MANAGER PROTOCOL ERROR POSSIBLE"
        ,x);
    }    
  }
  
  public synchronized void prepare()
    throws TransactionException
  {
    try
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
    catch (RuntimeException x)
    { 
      rollbackOnComplete();
      throw new TransactionException
        ("xid "+id+": Runtime exception preparing transaction- rollback forced"
        ,x);
    }    
  }
  
  public synchronized void rollback()
    throws TransactionException
  {
    try
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
          try
          { transaction.rollback();
          }
          catch (Exception x)
          { 
            log.log
              (Level.WARNING
              ,"Caught exception in subtransaction "+transaction.getId()
              +" rollback"
              ,x);
          }
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
          try
          { llrBranch.rollback();
          }
          catch (Exception x)
          { 
            log.log
              (Level.WARNING
              ,"Caught exception in llrBranch "
              +" rollback for xid "+id
              ,x);
          }
        }
      }
      else
      { throw new IllegalStateException("Cannot rollback in state "+state);
      }
    }
    catch (RuntimeException x)
    { 
      throw new TransactionException
        ("xid "+id+": Runtime exception rolling back transaction",x);
    }
  }
  
  public synchronized void complete()
  {
    for (Transaction transaction:subtransactions)
    { 
      if (transaction.getState()!=State.COMPLETED)
      { 
        TRANSACTION_LOCAL.set(parent);
        throw new IllegalStateException
          ("Transaction "+transaction+" must complete before this"
          +" transaction "+this+" can complete"
           );
      }
    }
 
    
    if (TRANSACTION_LOCAL.get()!=this)
    { 
      throw new IllegalStateException
        ("Transaction "+TRANSACTION_LOCAL.get()+" must complete before this"
         +" transaction "+this+" can complete"
        );
    }
    
    if (parent!=null && nesting==Nesting.PROPOGATE)
    {
      if (debug)
      {
        log.fine
          ("Transaction #"+getId()
          +" completed- returning control to parent transaction #"+parent.getId()
          );
      }

      TRANSACTION_LOCAL.set(parent);
      this.state=State.COMPLETED;
      return;
    }
    
    try
    {
      if (state!=State.COMMITTED 
          && state!=State.ABORTED
          )
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
            try
            { 
              log.log
                (Level.WARNING
                ,"Commit failure for xid "+id+" on commit during complete"
                ,x);
              rollback();
            }
            catch (TransactionException y)
            { 
              log.log
              (Level.WARNING
              ,"Rollback failure for xid "+id+" on failed commit during complete"
              ,y);
            }
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
            log.log
            (Level.WARNING
            ,"Rollback failure for xid "+id+" during complete"
            ,x);
          }
        }
    
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
        try
        { branch.complete();
        }
        catch (Exception x)
        {
          log.log
            (Level.WARNING
            ,"Branch complete for xid "+id+", branch "+branch+" threw exception"
            ,x);
        }
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
        try
        { llrBranch.complete();
        }
        catch (Exception x)
        {
          log.log
            (Level.WARNING
            ,"llrBranch complete for xid "+id+", branch "+llrBranch
            +" threw exception"
            ,x);
        }
      }
    }
    finally
    {
      TRANSACTION_LOCAL.set(parent);
    }
    state=State.COMPLETED;
    
  }
  
}
