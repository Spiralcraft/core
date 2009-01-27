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
import spiralcraft.command.CommandAdapter;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;

/**
 * <p>A Scenario that functions as a simple Task factory when the task() method
 *   is implemented.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class AbstractScenario<T extends Task>
  implements Scenario<T>
{

  protected final ClassLog log=ClassLog.getInstance(getClass());
  
  @Override
  public Command<AbstractScenario<T>, T> runCommand()
  { 
    return new CommandAdapter<AbstractScenario<T>,T>()
    {
      { setTarget(AbstractScenario.this);
      }
      
      @Override
      public void run()
      {
        T task=task();
        setResult(task);
        task.run();
      }
        
    };
  }

  @Override
  public abstract T task();

  @Override
  public void start()
    throws LifecycleException
  {
  }

  @Override
  public void stop()
    throws LifecycleException
  {    
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }

}
