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
 * <p>Provides information about Commands associated with a given type of 
 *   target
 * </p>
 * 
 * <p>A Commands is associated with a target by convention or mapping
 * </p>
 * 
 * <p>XXX Incomplete
 * </p>
 * 
 * @author mike
 * 
 */
public interface CommandMenu
{
  
  /**
   * @return Information about the Commands registered for the target,
   *   in order of their definition.
   */
  public CommandInfo[] listCommands();
  
  /**
   * 
   * @param alias The command alias
   * @return The CommandInfo that describes the command with the specified
   *    alias.
   */
  public CommandInfo getCommandInfo(String alias);
}
