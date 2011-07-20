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


import spiralcraft.common.ContextualException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.Type;
import spiralcraft.data.session.DataSessionFocus;
import spiralcraft.data.session.DataSession;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.WorkException;
import spiralcraft.data.transaction.WorkUnit;

import spiralcraft.lang.Assignment;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.task.Chain;
import spiralcraft.task.Task;

import spiralcraft.data.transaction.Transaction;

/**
 * <p>Provides a stateful typed DataSession Context that can be shared between 
 *   scenarios and that manages buffers for chained scenarios.
 * </p>
 *
 * @author mike
 *
 * @param <Tresult>
 */
public class Session
  extends Chain<Void,Void>
{

  
  protected ThreadLocalChannel<DataSession> sessionChannel
    =new ThreadLocalChannel<DataSession>
      (BeanReflector.<DataSession>getInstance(DataSession.class));
  
  protected DataSessionFocus dataSessionFocus;
  protected Type<? extends DataComposite> type;
  protected Expression<Type<? extends DataComposite>> typeX;
  protected Assignment<?>[] initialAssignments;
  protected Setter<?>[] initialSetters;
  protected boolean transactional;
  
  public Session()
  {
  }
  
  public void setType(Type<? extends DataComposite> type)
  { this.type=type;
  }
  
  public void setTypeX(Expression<Type<? extends DataComposite>> typeX)
  { this.typeX=typeX;
  }

  public void setInitialAssignments(Assignment<?>[] initialAssignments)
  { this.initialAssignments=initialAssignments;
  }
  
  public void setTransactional(boolean transactional)
  { this.transactional=transactional;
  }
  
  @Override
  protected Task task()
  {
    return new ChainTask()
    {
        
      @Override
      public void work()
      {
        if (transactional)
        {
          try
          {
            new WorkUnit<Void>()
            { 
              { 
                this.setDebug(Session.this.debug);
                this.setNesting(Transaction.Nesting.PROPOGATE);
                this.setRequirement(Transaction.Requirement.REQUIRED);
              }
              
              @Override
              protected Void run()
                throws WorkException
              { 
                try
                {
                  doWork();
                  if (getException()!=null)
                  { Transaction.getContextTransaction().rollbackOnComplete();
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
        else
        {
          try
          { doWork();
          }
          catch (InterruptedException x)
          { addException(x);
          }
        }
        
        
      }
      
      private void doWork()
        throws InterruptedException
      {
        sessionChannel.push(null);
        dataSessionFocus.reset();
        Setter.applyArray(initialSetters);
        
        super.work();
        sessionChannel.pop();
      }
    };
  }
  
  
  @Override
  protected Focus<?> bindExports(
    Focus<?> focusChain)
    throws ContextualException
  {
    if (typeX!=null)
    { 
      Channel<Type<? extends DataComposite>> channel
        =focusChain.bind(typeX);
      if (channel!=null)
      { type=channel.get();
      }
    }
    dataSessionFocus
      =new DataSessionFocus(focusChain,sessionChannel,type);
    initialSetters=Assignment.bindArray(initialAssignments, dataSessionFocus);
    if (type!=null)
    { focusChain=dataSessionFocus;
    }
    else
    {
      focusChain=focusChain.chain(focusChain.getSubject());
      focusChain.addFacet(dataSessionFocus);
    }
    return super.bindExports(focusChain);
  }

}
