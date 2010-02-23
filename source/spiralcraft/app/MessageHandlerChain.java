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

import java.util.List;

public class MessageHandlerChain
{

  private MessageHandler next;
  private MessageHandlerChain nextChain;
  
  /**
   * Create a terminal link
   */
  public MessageHandlerChain()
  { 
  }
  
  /**
   * Create a chain composed of the handlers in the array starting with the
   *   handler at the specified index.
   * 
   * @param handlers
   * @param start
   */
  public MessageHandlerChain(MessageHandler[] handlers,int start)
  { 
    if (start<handlers.length)
    { 
      next=handlers[start];
      nextChain=new MessageHandlerChain(handlers,start+1);
    }
  
  }
  
  /**
   * Create a chain composed of the handlers in the specified list
   */
  public MessageHandlerChain(List<MessageHandler> handlers)
  { this(handlers.toArray(new MessageHandler[handlers.size()]),0);
  }
  
  /**
   * Add the specified handler to the end of the chain
   * 
   * @param handler
   */
  public void add(MessageHandler handler)
  { 
    if (next!=null)
    { nextChain.add(handler);
    }
    else
    { 
      next=handler;
      nextChain=new MessageHandlerChain();
    }
  }
  
  /**
   * <p>Handle the Message. This will be called twice- once before children are
   *   messaged and once afterwards.
   * </p>
   * 
   * @param context The EventContext that holds the Element's state
   * @param message The message
   * @param postOrder Whether this call is before or after the message has
   *                    propagated to children 
   */
  public void handleMessage
    (MessageContext context
    ,Message message
    ,Component component
    )
  { 
    if (next!=null)
    { next.handleMessage(context,message,nextChain);
    }
    else
    { context.relayMessage(message);
    }
  }
  
}
