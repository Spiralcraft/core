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
package spiralcraft.task;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;
import spiralcraft.task.Task;
import spiralcraft.util.ArrayUtil;

/**
 * Conditionally evaluates an Expression and logs the result
 *  
 * @author mike
 *
 */
public class Debug<Tcontext>
  extends Scenario<Tcontext,Void>
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
        if (messageChannel!=null && Boolean.TRUE.equals(condition))
        {
          Object message=messageChannel.get();
          if (message!=null && message.getClass().isArray())
          { 
            log.debug(messageX.getText()
              +" := ["+ArrayUtil.format(message,",","")+"]"
              );
          }
          else
          { log.debug(messageX.getText()+" := ["+message+"]");
          }
        }
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
