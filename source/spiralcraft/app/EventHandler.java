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
package spiralcraft.app;

public interface EventHandler
{
  /**
   * <p>Handle the Event. Handlers are used to bind events to
   *   Component functionality.
   * </p>
   * 
   *
   * @param context The MessageContext
   * @param event The event
   */
  void handleEvent
    (MessageContext context
    ,Event event
    );
  
  /**
   * <p>The type of event this handler supports, or null if all 
   *   message types will be sent through this handler.
   * </p>
   * 
   * @return
   */
  Event.Type getType();
  
}
