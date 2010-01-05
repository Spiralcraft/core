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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;



/**
 * <p>A Scenario which runs an arbitrary Command, and provides access 
 *   to the completed Command as the result.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 * @param <R>
 */
public class Exec<T,C,R>
  extends Chain<C,Command<T,C,R>>
{
  
  private Channel<Command<T,C,R>> targetCommandChannel;
  private Expression<Command<T,C,R>> commandX;
  private ThreadLocalChannel<Command<T,C,R>> chainCommand;
  private Binding<R> onComplete;
  
  @Override
  public CommandTask task()
  { return new CommandTask();
  }
  
  /**
   * Provide an expression to resolve the Command object
   */
  public void setCommandX(Expression<Command<T,C,R>> commandX)
  { this.commandX=commandX;
  }
  
  public void setOnComplete(Binding<R> onComplete)
  { this.onComplete=onComplete;
  }
    
  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws BindException
  { 

    targetCommandChannel=focusChain.bind(commandX);
    chainCommand=new ThreadLocalChannel<Command<T,C,R>>
      (targetCommandChannel.getReflector());
    focusChain=focusChain.chain(chainCommand);
    if (onComplete!=null)
    { onComplete.bind(focusChain);
    }
    super.bindChildren(focusChain);
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
    extends ChainTask
  {
    private volatile Command<T,C,R> command;

    public CommandTask()
    { 
    }

    public Command<?,C,R> getCommand()
    { return command;
    }

    @Override
    public void work()
      throws InterruptedException
    { 
      chainCommand.push(null);
      try
      {
        try
        {
          command=targetCommandChannel.get();
          chainCommand.set(command);

          
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
      
        super.work();
        
        if (onComplete!=null)
        { onComplete.get();
        }
      }
      finally
      { chainCommand.pop();
      }
    }

  }
}