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
 * <p>Creates a new instance of a Command, resolving contextual information
 *   at the time of command creation. 
 * </p>
 * 
 * 
 * <p>The CommandFactory usually has an application level (long duration) 
 *   lifecycle, whereas a Command usually has an operation level (short
 *   duration) lifecycle.
 * </p>
 * 
 * <p>The CommandFactory is primarily intended to decouple the mechanism
 *   of Command instance creation from the client interface components
 *   that trigger Command execution
 * </p>
 *
 */
public interface CommandFactory<Ttarget,Tresult>
{
  /**
   * @return A new Command object
   */
  Command<Ttarget,Tresult> command();
  
  /**
   * 
   * @return Whether the Command supplied by this CommandFactory can be
   *   executed in the supplied context.
   */
  boolean isCommandEnabled();
  
}
