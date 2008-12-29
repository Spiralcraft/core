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
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>Executes a command
 * </p>
 * 
 * @author mike
 *
 * @param <I> The command target type
 * @param <R> The command result type
 */
public class CommandScenario<I,R>
  implements Scenario
{
  private ThreadLocalChannel<I> item; 
  private Channel<I> targetChannel;
  
  private Channel<Command<?,R>> commandChannel;  
  
  public class CommandTask
    extends AbstractTask
  {
    private I target;
    private volatile Command<?,R> completedCommand;

    public CommandTask(I target)
    { this.target=target;
    }

    public Command<?,R> getCompletedCommand()
    { return completedCommand;
    }

    @Override
    public void execute()
    {
      item.push(target);
      try
      { 
        completedCommand=commandChannel.get();
        completedCommand.execute();
      }
      finally
      { item.pop();
      }
    }

  }  
  
  @Override
  public Command<? extends Scenario, ?> runCommand()
  {
    return new CommandAdapter<CommandScenario<I,R>,CommandTask>()
    {

      @Override
      protected void run()
      {
        CommandTask task=task();
        task.run();
        setResult(task);
        
        // TODO Auto-generated method stub
        
      }
    };
  }

  @Override
  public CommandTask task()
  { return new CommandTask(targetChannel.get());
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
  public void bind(
    Focus<?> focusChain)
    throws BindException
  {
  // TODO Auto-generated method stub

  }

  @Override
  public Focus<?> getFocus()
  {
    // TODO Auto-generated method stub
    return null;
  }

}


