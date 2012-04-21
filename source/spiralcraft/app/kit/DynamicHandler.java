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
package spiralcraft.app.kit;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.common.ContextualException;
import spiralcraft.util.tree.Order;
import spiralcraft.lang.Focus;
import spiralcraft.util.thread.ThreadLocalStack;

/**
 * <p>Accepts a set of late-bound handlers to be run in the context of this
 *   handler 
 * </p>
 * 
 * @author mike
 *
 */
public class DynamicHandler
  implements MessageHandler
{

  private StandardMessageHandlerChain handler;
  private Focus<?> focus;
  private ThreadLocalStack<MessageHandlerChain> defaultChain
    =new ThreadLocalStack<MessageHandlerChain>();
  private Order order=Order.IN;
  
  public synchronized void addHandler(MessageHandler handler)
  { 
    if (this.handler==null)
    { this.handler=new StandardMessageHandlerChain(handler);
    }
    else
    { this.handler.chain(handler);
    }
  }
    
  /**
   * Run the dynamic chain  as a subroutine and return 
   * @param subchain
   */
  public void setOrder(Order order)
  { this.order=order;
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  {
    this.focus=focusChain;
    return focus;
  }

  @Override
  public void handleMessage(
    Dispatcher context,
    Message message,
    MessageHandlerChain chain)
  { 
    switch (order)
    {
      case PRE:
        handler.handleMessage(context,message);
        chain.handleMessage(context,message);
        break;
      case IN:
        defaultChain.push(chain);
        try
        { handler.handleMessage(context,message);
        }
        finally
        { defaultChain.pop();
        }
        break;
      case POST:
        chain.handleMessage(context,message);
        handler.handleMessage(context,message);
        break;
    }
  }

  public void completeBind()
    throws ContextualException
  { 
    if (handler==null)
    { handler=new StandardMessageHandlerChain(new DefaultHandler());
    }
    else
    { handler.chain(new DefaultHandler());
    }
    handler.bind(focus);
  }
  
  class DefaultHandler
    implements MessageHandler
  {

    @Override
    public Focus<?> bind(
      Focus<?> focusChain)
      throws ContextualException
    { return focusChain;
    }

    @Override
    public void handleMessage(
      Dispatcher context,
      Message message,
      MessageHandlerChain chain)
    { 
      if (order==Order.IN)
      { defaultChain.get().handleMessage(context,message);
      }
    }
  }
  
}
