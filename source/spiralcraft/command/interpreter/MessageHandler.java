//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.command.interpreter;

/**
 * Interface for Commands to pass messages back to a user agent
 */
public interface MessageHandler
{
  /**
   * Pass a message back to the user agent. Each Object in the supplied
   *   Object[] is represents a unit of the message. By convention,
   *   simple user agents should represent these units as lines of text,
   *   converting the Objects to Strings using the toString() method.
   */
  void handleMessage(Object[] messageLines);
}
