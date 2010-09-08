//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.app.spi;

import java.util.ArrayList;
import java.util.HashMap;

import spiralcraft.app.Message;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.common.Lifecycle;

import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;

public class MessageHandlerSupport
  implements Lifecycle,Contextual
{
  
  private MessageHandler[] handlers;
  
  private HashMap<Message.Type,MessageHandlerChain> chainMap
    =new HashMap<Message.Type,MessageHandlerChain>();
  
  private MessageHandlerChain standardChain;
  private boolean started;
 

  @Override
  public Focus<?> bind(Focus<?> focusChain)
  { 
    recomputeChains();
    
    return focusChain;
  }
  
  @Override
  public void start()
  { 
    started=true;
    recomputeChains();
    
  }
  
  @Override
  public void stop()
  { started=false;
  }
  
  public void set(MessageHandler[] handlers)
  { 
    this.handlers=handlers;
    
    if (started)
    { recomputeChains();
    }
  }
  
  
  private void recomputeChains()
  {
    ArrayList<MessageHandler> standardHandlers
      =new ArrayList<MessageHandler>();
    
    HashMap<Message.Type,ArrayList<MessageHandler>> typeHandlers
      =new HashMap<Message.Type,ArrayList<MessageHandler>>();
    
    if (handlers!=null)
    {
      for (MessageHandler handler:handlers)
      { 
        ArrayList<MessageHandler> handlerList;
        if (handler.getType()!=null)
        { 
          handlerList=typeHandlers.get(handler.getType());
          if (handlerList==null)
          { 
            handlerList=new ArrayList<MessageHandler>();
            typeHandlers.put(handler.getType(),handlerList);
          }
        }
        else
        { handlerList=standardHandlers;
        }
      
        handlerList.add(handler);
      }
    }
    
    standardChain=new MessageHandlerChain(standardHandlers);
    
    for (Message.Type type : typeHandlers.keySet())
    {
      MessageHandlerChain typeChain
        =new MessageHandlerChain(standardHandlers);
      
      for (MessageHandler handler : typeHandlers.get(type))
      { typeChain.add(handler);
      }
      chainMap.put
        (type
        ,typeChain
        );
    }
  }
  
  public MessageHandlerChain getChain(Message.Type messageType)
  {
    if (standardChain==null)
    { recomputeChains();
    }
    
    MessageHandlerChain chain=chainMap.get(messageType);
    if (chain!=null)
    { return chain;
    }
    else
    { return standardChain;
    }  
  }
  
}
