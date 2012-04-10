//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.app;

/**
 * <p>A reference to a state in a state tree through which messages can be sent
 * </p>  
 * 
 * @author mike
 *
 */
public interface Pipe
{
  /**
   * Send a message to the referenced state
   * 
   * @param message
   */
  void message(Message message);
  
  /**
   * Send an asynchronous message to the referenced state
   * 
   * @param message
   */
  void messageAsync(Message message);
}
