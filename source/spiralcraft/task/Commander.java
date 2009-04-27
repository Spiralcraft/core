//
// Copyright (c) 2008,2009 Michael Toth
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
import spiralcraft.command.BoundCommandFactory;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;



/**
 * <p>A leaf Scenario which runs an arbitrary Command, and provides access 
 *   to the completed Command as the result.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 * @param <R>
 */
public class Commander<T,R>
  extends Scenario<Commander<T,R>.CommandTask,Command<T,R>>
{
  
  private BoundCommandFactory<T,R> commandFactory;
  
  @Override
  public CommandTask task()
  { return new CommandTask(commandFactory.command());
  }
  
  /**
   * Provide an expression to resolve the Command
   */
  public void setCommandX(Expression<Command<T,R>> commandX)
  { this.commandFactory=new BoundCommandFactory<T,R>(commandX);
  }
  
  @Override
  protected Focus<?> bindChildren(Focus<?> focusChain)
    throws BindException
  { return commandFactory.bind(super.bindChildren(focusChain));
  }
  
  /**
   * <p>A Task adapter which runs an arbitrary Command, and provides access to 
   *   the completed Command via the addResult() callback to allow for 
   *   inspection of results/error/etc.
   * </p>
   * 
   * @author mike
   *
   * @param <T>
   * @param <R>
   */
  public class CommandTask
    extends AbstractTask<Command<T,R>>
  {
    private volatile Command<T,R> command;

    public CommandTask(Command <T,R> command)
    { this.command=command;
    }

    public Command<?,R> getCommand()
    { return command;
    }

    @Override
    public void work()
    { 
      try
      {
        command.execute();
        addResult(command);
      }
      catch (Exception x)
      { addException(x);
      }
    }

  }
}