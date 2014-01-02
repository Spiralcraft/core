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



import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.lang.Binding;
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
public class Sequence<Tresult>
  extends Scenario<Void,Tresult>
{

  
  protected Scenario<?,?>[] scenarios;
  protected Binding<Tresult> resultX;

  { importContext=false;
  }
  
  public Sequence()
  {
  }
  
  public Sequence(Scenario<?,?>[] scenarios)
  { setScenarios(scenarios);
  }
  
  public void setScenarios(Scenario<?,?>[] scenarios)
  { this.scenarios=scenarios;
  }
  
  public void addScenario(Scenario<?,?> scenario)
  { 
    scenarios
      =scenarios!=null
        ?ArrayUtil.append(scenarios, scenario)
        :new Scenario<?,?>[] {scenario}
        ;
  }
  
  public void setResultX(Binding<Tresult> resultX)
  { 
    this.resultX=resultX;
    this.storeResults=true;
  }
  
  public void setSequence(Scenario<?,?>[] scenarios)
  { this.scenarios=scenarios;
  }
  
  @Override
  protected Task task()
  {
    
    return new CommandTask()
    {
      { addCommandAsResult=(resultX==null);
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
        if (resultX!=null)
        { 
          addResult(resultX.get());
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

    if (resultX!=null)
    { 
      resultX.bind(focusChain);
      resultReflector=resultX.getReflector();
    }
    
    super.bindChildren(focusChain);
  }

}
