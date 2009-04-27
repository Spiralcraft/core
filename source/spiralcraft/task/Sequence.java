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

import java.util.List;

import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

/**
 * <p>Run a set of Scenarios in order of their declaration
 * </p>
 * 
 * <p>The results of the individual Scenarios are presented as completed 
 *   TaskCommand objects. 
 * </p>
 * 
 * @author mike
 *
 * @param <Tresult>
 */
public class Sequence<Tresult>
  extends Scenario<Task,List<TaskCommand<Task,Tresult>>>
{

  protected Scenario<Task,Tresult>[] scenarios;
  
  
  public void setScenarios(Scenario<Task,Tresult>[] scenarios)
  { this.scenarios=scenarios;
  }
  
  @Override
  protected Task task()
  {
    return new AbstractTask<TaskCommand<Task,Tresult>>()
    {
        
      @Override
      public void work()
      {
        for (Scenario<Task,Tresult> scenario: scenarios)
        { 
          TaskCommand<Task,Tresult> command
            =scenario.command();
          if (debug)
          { log.fine("Executing "+command);
          }
          command.execute();
          addResult(command);
          if (command.getException()!=null)
          { addException(command.getException());
          }
        }
      }
    };
  }

  @Override
  public void start()
    throws LifecycleException
  {
    super.start();
    for (Scenario<Task,Tresult> scenario: scenarios)
    { scenario.start();
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    for (Scenario<Task,Tresult> scenario: scenarios)
    { scenario.stop();
    }
    super.stop();
  }
  
  
  @Override
  protected Focus<?> bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    focusChain=super.bindChildren(focusChain);
    
    for (Scenario<Task,Tresult> scenario: scenarios)
    { scenario.bind(focusChain);
    }
    return focusChain;
  }

}
