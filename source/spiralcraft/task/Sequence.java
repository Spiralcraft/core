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

import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.lang.Focus;
import spiralcraft.util.ArrayUtil;

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
public class Sequence
  extends Scenario<Void,List<?>>
{

  
  protected Scenario<?,?>[] scenarios;

  { importContext=false;
  }
  
  public void setScenarios(Scenario<?,?>[] scenarios)
  { this.scenarios=scenarios;
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
  protected Task task()
  {
    
    return new CommandTask()
    {
      { addCommandAsResult=true;
      }
      
      @Override
      public void work()
        throws InterruptedException
      {
        
        if (scenarios!=null)
        {
          for (Scenario<?,?> scenario: scenarios)
          { 
            command=scenario.command();
            super.work();
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
    Lifecycler.start(Lifecycler.group(scenarios));
    
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    Lifecycler.stop(Lifecycler.group(scenarios));
    super.stop();
  }
  
  
  @Override
  protected void bindChildren(
    Focus<?> focusChain)
    throws ContextualException
  {
    
    if (scenarios!=null)
    {
      for (Scenario<?,?> scenario: scenarios)
      { scenario.bind(focusChain);
      }
    }
    super.bindChildren(focusChain);
  }

}
