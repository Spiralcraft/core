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

import spiralcraft.lang.Contextual;

public interface MessageHandlerChain
  extends Contextual
{
  
  /**
   * Add the specified handler to the end of the chain
   * 
   * @param handler
   */
  void chain(MessageHandler handler);
  
  /**
   * <p>Handle the Message. This will be called twice- once before children are
   *   messaged and once afterwards.
   * </p>
   * 
   * @param dispatcher The Dispatcher that holds the Element's state
   * @param message The message
   */
  void handleMessage
    (Dispatcher dispatcher
    ,Message message
    );
  
}
