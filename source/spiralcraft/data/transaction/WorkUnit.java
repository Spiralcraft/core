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

import spiralcraft.log.ClassLog;

/**
 * 
 * @author mike
 *
 * @param <Tresult>
 */
public abstract class WorkUnit<Tresult>
{
  private static final ClassLog log
    =ClassLog.getInstance(WorkUnit.class);
  
  protected Transaction transaction;
  protected Transaction.Nesting nesting
    =Transaction.Nesting.PROPOGATE;
  protected Transaction.Requirement requirement
    =Transaction.Requirement.REQUIRED;
  
  private boolean newTransaction;
  private boolean debug;

  public WorkUnit()
  {
  }
  
  public WorkUnit(Transaction.Requirement requirement,Transaction.Nesting nesting)
  {
    this.requirement=requirement;
    this.nesting=nesting;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public void setNesting(Transaction.Nesting nesting)
  { this.nesting=nesting;
  }
  
  public void setRequirement(Transaction.Requirement requirement)
  { this.requirement=requirement;
  }
  
  public final Tresult work()
    throws TransactionException
  { 
    // XXX Deal with no-transaction case
    
    transaction
      =Transaction.getContextTransaction();
  
    if (transaction==null && requirement==Transaction.Requirement.REQUIRED)
    { 
      transaction=
        Transaction.startContextTransaction(nesting);
      
      newTransaction=true;
    }
    
    try
    {
    
    
      if (newTransaction)
      {
        if (debug)
        { log.fine("Started new transaction");
        }
      }
      else
      {
        if (debug)
        { log.fine("Obtained existing transaction");
        }
      }
      if (debug && transaction!=null)
      { transaction.setDebug(true);
      }
    

      Tresult ret=run();

      if (newTransaction)
      { 
        if ((transaction.getState()==Transaction.State.STARTED
            || transaction.getState()==Transaction.State.PREPARED
            )
            && !transaction.getRollbackOnComplete()
            )
        { transaction.commit();
        }
      }
      return ret;
    }
    catch (WorkException x)
    {
      if (transaction!=null)
      {
        if (debug)
        { log.fine("Rolling back on "+x.getCause());
        }
        transaction.rollback();
      }
      throw x;
    }
    catch (RuntimeException x)
    { 
      if (transaction!=null)
      {
        if (debug)
        { log.fine("Rolling back on "+x);
        }
        transaction.rollback();
      }
      throw x;
    }
    catch (Error x)
    {
      if (transaction!=null)
      {
        if (debug)
        { log.fine("Rolling back on "+x);
        }
        transaction.rollback();
      }
      throw x;
    }
    finally
    { 
      if (newTransaction)
      { transaction.complete();
      }
    }    
    
    
  }
  
  
  protected abstract Tresult run()
    throws WorkException;
  
}
