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

/**
 * <p>A Task adapter which runs a Command, and provides access to the
 *   completed Command to allow for inspection of results/error/etc.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 * @param <R>
 */
public class CommandTask<T,R>
  extends AbstractTask
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
  { command.execute();
  }

} 