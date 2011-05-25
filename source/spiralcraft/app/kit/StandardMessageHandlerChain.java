//
// Copyright (c) 2011 Michael Toth
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

import spiralcraft.lang.Focus;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.MessageHandlerChain;


import spiralcraft.app.Message;
import spiralcraft.common.ContextualException;

public class StandardMessageHandlerChain
  implements MessageHandlerChain
{
 
  private MessageHandler nextHandler;
  private MessageHandlerChain nextChain;
  
  public StandardMessageHandlerChain(MessageHandler handler)
  { this.nextHandler=handler;
  }
  
  @Override
  public void handleMessage(Dispatcher context,Message message)
  { 
    if (nextHandler!=null)
    { nextHandler.handleMessage(context,message,nextChain);
    }
  }

  @Override
  public void chain(MessageHandler handler)
  {
    if (nextHandler==null)
    { nextHandler=handler;
    }
    else if (nextChain==null)
    { nextChain=new StandardMessageHandlerChain(handler);
    }
    else
    { nextChain.chain(handler);
    }
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  { 
    if (nextHandler!=null)
    { focusChain=nextHandler.bind(focusChain);
    }
    if (nextChain!=null)
    { focusChain=nextChain.bind(focusChain);
    }
    return focusChain;
  }

}
