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
package spiralcraft.app.kit;

import java.util.List;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;

public class StandardMessageHandlerChain
  implements MessageHandlerChain
{

  private MessageHandler next;
  private StandardMessageHandlerChain nextChain;
  
  /**
   * Create a terminal link
   */
  public StandardMessageHandlerChain()
  { 
  }
  
  /**
   * Create a chain composed of the handlers in the array starting with the
   *   handler at the specified index.
   * 
   * @param handlers
   * @param start
   */
  public StandardMessageHandlerChain(MessageHandler[] handlers,int start)
  { 
    if (start<handlers.length)
    { 
      next=handlers[start];
      nextChain=new StandardMessageHandlerChain(handlers,start+1);
    }
  
  }
  
  /**
   * Create a chain composed of the handlers in the specified list
   */
  public StandardMessageHandlerChain(List<MessageHandler> handlers)
  { this(handlers.toArray(new MessageHandler[handlers.size()]),0);
  }
  
  /**
   * Add the specified handler to the end of the chain
   * 
   * @param handler
   */
  @Override
  public void chain(MessageHandler handler)
  { 
    if (next!=null)
    { nextChain.chain(handler);
    }
    else
    { 
      next=handler;
      nextChain=new StandardMessageHandlerChain();
    }
  }
  
  /**
   * <p>Handle the Message. This will be called twice- once before children are
   *   messaged and once afterwards.
   * </p>
   * 
   * @param dispatcher The Dispatcher that holds the Element's state
   * @param message The message
   */
  @Override
  public void handleMessage
    (Dispatcher dispatcher
    ,Message message
    )
  { 
    if (next!=null)
    { next.handleMessage(dispatcher,message,nextChain);
    }
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  {
    if (next!=null)
    { return next.bind(focusChain);
    }
    else
    { return focusChain;
    }
  }
}
