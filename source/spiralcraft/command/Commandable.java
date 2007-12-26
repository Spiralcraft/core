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

/**
 * An interface which executes commands against a specific target.
 * 
 * @author mike
 * 
 */
public interface Commandable<Ttarget>
{
  
  /**
   * 
   * @return Information about the commands registered for the target
   *   associated with this Commandable. 
   */
  public CommandInfo[] getCommands();
  
  
  /**
   * <p>Resolve the command target and execute the command asynchronously.
   * </p>
   * 
   * <p>The actual command target and the actual execution may be in a
   *   different context. This method may return before the command is
   *   complete.
   * </p>
   * 
   * @param command
   */
  public void executeCommand(Command<Ttarget,?> command);
  
}
