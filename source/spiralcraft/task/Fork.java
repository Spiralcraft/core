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


import java.util.LinkedList;
import java.util.List;

import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.util.ArrayUtil;

/**
 * <p>Run a set of Scenarios in parallel
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
public class Fork
  extends Scenario<Void,List<?>>
{

  protected Scenario<?,?>[] scenarios;
  protected int multiplier=1;
  
  /**
   * 
   * @param scenarios the Scenarios to execute in parallel
   */
  public void setScenarios(Scenario<?,?>[] scenarios)
  { this.scenarios=scenarios;
  }
  
  /**
   * 
   * @param multiplier The number of times the scenario group will be forked-
   *   defaults to 1
   */
  public void setMultiplier(int multiplier)
  { this.multiplier=multiplier;
  }
  
  public void addScenario(Scenario<?,?> scenario)
  { 
    scenarios
      =scenarios!=null
        ?ArrayUtil.append(scenarios, scenario)
        :new Scenario[] {scenario}
        ;
  }
  
  @Override
  protected ParallelTask<CommandTask> task()
  {
    final List<CommandTask> taskList=taskList();
    return new ParallelTask<CommandTask>(taskList);
  }
  
  private LinkedList<CommandTask> taskList()
  { 
    LinkedList<CommandTask> taskList=new LinkedList<CommandTask>();
    for (int i=0;i<multiplier;i++)
    {
      for (final Scenario<?,?> tc : scenarios)
      { 
        CommandTask commandTask
          =new CommandTask()
        {
          { 
            addResult=true;
            command=tc.command();
          }
          
        };
        taskList.add(commandTask);
      }
    }
    return taskList;
  }

  @Override
  public void start()
    throws LifecycleException
  {
    super.start();
    for (Scenario<?,?> scenario: scenarios)
    { scenario.start();
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    for (Scenario<?,?> scenario: scenarios)
    { scenario.stop();
    }
    super.stop();
  }
  
  
  @Override
  protected void bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    
    
    for (Scenario<?,?> scenario: scenarios)
    { scenario.bind(focusChain);
    }
    super.bindChildren(focusChain);
  }

}
