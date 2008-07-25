//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.command;

import java.util.List;

/**
 * <p>Executes multiple commands as a unit. Holds the result of last successful
 *   command and the exception from a failed command, which normally interrupts
 *   execution.
 * </p>
 * 
 * @author mike
 *
 * @param <Ttarget>
 * @param <Tresult>
 */
@SuppressWarnings("unchecked") // Non type specific Command ops
public class CommandBlock
  extends CommandAdapter
{

  private List<Command> commands;
  
  public CommandBlock()
  {
  }
  
  public CommandBlock(List<Command> commands)
  { setCommands(commands);
  }
  
  /**
   * The List of commands referred to by this block, which may be in various
   *   stages of execution.
   * 
   * @return
   */
  public List<Command> getCommands()
  { return commands;
  }
  
  /**
   * @param commands The List of commands that will be executed by this block
   */
  public void setCommands(List<Command> commands)
  { 
    
    if (isStarted())
    { 
      throw new IllegalStateException
        ("CommandBlock already running, cannot replace commands.");
    }
    this.commands=commands;
  }
  
  @Override
  protected void run()
  {
    if (commands!=null)
    {
      for (Command command: commands)
      {
        command.execute();
        setResult(command.getResult());
        Exception x=command.getException();
        if (x!=null)
        { 
          setException(x);
          break;
        }
      }
    }
  // TODO Auto-generated method stub

  }

}
