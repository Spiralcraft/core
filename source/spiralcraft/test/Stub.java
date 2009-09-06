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
package spiralcraft.test;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Task;

/**
 * Provides for the generation of a TestResult inside another arbitrary
 *   scenario.
 * 
 * @author mike
 *
 */
public class Stub
  extends Test
{

  protected Expression<Object> messageX;
  protected Channel<Object> messageChannel;
  protected Expression<Boolean> conditionX;
  protected Channel<Boolean> conditionChannel;
  

  
  public void setMessageX(Expression<Object> messageX)
  { this.messageX=messageX;
  }
  
  public void setConditionX(Expression<Boolean> conditionX)
  { this.conditionX=conditionX;
  }
  
  @Override
  protected Task task()
  {
    return new AbstractTask()
    {

      @Override
      protected void work()
        throws InterruptedException
      { 
        if (debug)
        { log.log(Level.FINE,this+": executing");
        }
        Boolean condition=conditionChannel!=null?conditionChannel.get():true;
        Object message=messageChannel!=null?messageChannel.get():null;
        
        TestResult result
          =new TestResult
             (name
             ,Boolean.TRUE.equals(condition)
             ,message!=null?message.toString():null
             );
        
        if (testGroup!=null)
        { testGroup.addTestResult(result);
        }
        if (throwFailure && !result.getPassed())
        { addException(new TestFailedException(result));
        }
        addResult
          (result
          );
      }
    };    
  }

  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws BindException
  {  
    if (messageX!=null)
    { messageChannel=focusChain.bind(messageX);
    }
    if (conditionX!=null)
    { conditionChannel=focusChain.bind(conditionX);
    }
    super.bindChildren(focusChain);
  }
}
