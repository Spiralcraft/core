//
// Copyright (c) 1998,2008 Michael Toth
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
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

/**
 * <p>Executes a command (incomplete)
 * </p>
 * 
 * @author mike
 *
 * @param <I> The command target type
 * @param <R> The command result type
 */
public class CommandScenario<I,R>
  implements Scenario<CommandTask<I,R>>
{  
  private Channel<Command<I,R>> commandChannel;  
  
  @Override
  public Command<CommandScenario<I,R>,CommandTask<I,R>> runCommand()
  {
    return new CommandAdapter<CommandScenario<I,R>,CommandTask<I,R>>()
    {

      @Override
      protected void run()
      {
        CommandTask<I,R> task=task();
        task.run();
        setResult(task);
      }
    };
  }

  @Override
  public CommandTask<I,R> task()
  { return new CommandTask<I,R>(commandChannel.get());
  }

  @Override
  public void start()
    throws LifecycleException
  {
  // TODO Auto-generated method stub

  }

  @Override
  public void stop()
    throws LifecycleException
  {
  // TODO Auto-generated method stub

  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { return focusChain;

  }
  
}


