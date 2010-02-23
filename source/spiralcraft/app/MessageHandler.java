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

public interface MessageHandler
{
  /**
   * <p>Handle the Message. Handlers are used to bind messages to
   *   Component functionality.
   * </p>
   * 
   * <p>The handler should call 
   *   chain.handleMessage(context,message,this) to continue processing
   *   the message, intercept the message by doing nothing, or throw 
   *   a RuntimeException.
   * </p>
   *
   * @param context The MessageContext 
   * @param message The message
   * @param chain  The MessageHandlerChain to run in context
   */
  void handleMessage
    (MessageContext context
    ,Message message
    ,MessageHandlerChain chain
    );
  
  /**
   * <p>The type of message this handler supports, or null if all 
   *   message types will be sent through this handler.
   * </p>
   * 
   * @return
   */
  Message.Type getType();
  
}
