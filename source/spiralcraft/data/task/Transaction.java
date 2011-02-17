//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.data.task;



import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.WorkException;
import spiralcraft.data.transaction.WorkUnit;

import spiralcraft.task.Chain;
import spiralcraft.task.Task;

import spiralcraft.data.transaction.Transaction.Nesting;
import spiralcraft.data.transaction.Transaction.Requirement;

/**
 * <p>Provides a stateful typed DataSession Context that can be shared between 
 *   scenarios and that manages buffers for chained scenarios.
 * </p>
 *
 * @author mike
 *
 * @param <Tresult>
 */
public class Transaction
  extends Chain<Void,Void>
{


  protected Nesting nesting
    =Nesting.PROPOGATE;
  protected Requirement requirement
    =Requirement.REQUIRED;
  
  class TransactionTask
    extends ChainTask
  {
  
    @Override
    public void work()
      throws InterruptedException
    {
      
      try
      {
        new WorkUnit<Void>()
        { 
          { 
            this.setDebug(Transaction.this.debug);
            this.setNesting(Transaction.this.nesting);
            this.setRequirement(Transaction.this.requirement);
          }
          @Override
          protected Void run()
            throws WorkException
          { 
            try
            {
              TransactionTask.super.work();
              if (TransactionTask.this.exception!=null)
              { rollbackOnComplete();
              }
              return null;
            }
            catch (InterruptedException x)
            { throw new WorkException("Transaction task interrupted",x);
            }
              
          }
  
        }.work();
      }
      catch (TransactionException x)
      { addException(x);
      }
    }
  }
  
  /**
   * Convenience method to allow children to rollback this transaction on
   *   completion.
   */
  public void rollbackOnComplete()
  { 
    spiralcraft.data.transaction.Transaction tx
      =spiralcraft.data.transaction.Transaction.getContextTransaction();
    if (tx!=null)
    { tx.rollbackOnComplete();
    }
  }
  
  @Override
  protected Task task()
  { return new TransactionTask();
  }
  }
