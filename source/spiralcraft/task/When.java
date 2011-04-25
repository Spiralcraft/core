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

import spiralcraft.command.Command;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;
import spiralcraft.task.Task;

/**
 * Conditionally executes another Scenario 
 *  
 * @author mike
 *
 */
public class When
  extends Scenario<Void,Void>
{

  protected Scenario<?,?> scenario;
  protected Expression<Boolean> conditionX;
  protected Channel<Boolean> conditionChannel;
  

  
  public void setScenario(Scenario<?,?> scenario)
  { this.scenario=scenario;
  }
  
  public void setX(Expression<Boolean> conditionX)
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
        if (Boolean.TRUE.equals(condition))
        {
          if (debug)
          { 
            log.debug
              ("Condition "+conditionX.getText()
              +" passed, running child scenario");
          }
          try
          {
            Command<?,?,?> command=scenario.command();
            command.execute();
            addResult(command);
            if (command.getException()!=null)
            { 
              addException(command.getException());
              return;
            }
          }
          catch (Exception x)
          { 
            addException(x);
            return;
          }
        }
      }
    };    
  }

  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws ContextualException
  {  

    if (conditionX!=null)
    { conditionChannel=focusChain.bind(conditionX);
    }
    scenario.bind(focusChain);
    super.bindChildren(focusChain);
  }
}
