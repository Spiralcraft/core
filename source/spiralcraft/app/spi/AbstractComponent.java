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


import spiralcraft.app.Component;
import spiralcraft.app.Container;
import spiralcraft.app.Message;
import spiralcraft.app.MessageContext;
import spiralcraft.app.State;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>Basic implementation of a component which uses a set of MessageHandlers
 *   to to handle incoming Messages.
 * </p>
 * 
 * @author mike
 *
 */
public class AbstractComponent
  implements Component
{
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level debugLevel=ClassLog.getInitialDebugLevel(getClass(),null);
  
  protected final MessageHandlerSupport handlers
    =new MessageHandlerSupport();
  
  protected Container parent;
  protected boolean bound=false;
  
  public void setParent(Container parent)
  { this.parent=parent;
  }
  
  public Container getParent()
  { return parent;
  }
  
  @Override
  public void message
    (MessageContext context
    ,Message message
    )
  { 
    handlers.getChain(message.getType())
      .handleMessage(context,message,this);
  }

  @Override
  public Container asContainer()
  { return null;
  }

  /**
   * <p>Override to create a new State.
   */
  @Override
  public State createState(State parentState)
  { return new SimpleState(-1,parentState);
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    focusChain=bindImports(focusChain);
    focusChain=bindExports(focusChain);
    return focusChain;
  }

  @Override
  public void start()
    throws LifecycleException
  { handlers.start();    
  }

  @Override
  public void stop()
    throws LifecycleException
  { handlers.stop();
  }

  protected Focus<?> bindImports(Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }

  protected Focus<?> bindExports(Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }
  
  public int getStateDepth()
  { return 1;
  }

  protected State getState(MessageContext context)
  { return context.getState();
  }
  
  public int getStateDistance(Class<?> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return getStateDepth()-1;
    }
    else if (parent!=null)
    { 
      int parentDist=parent.getStateDistance(clazz);
      if (parentDist>-1)
      { return parentDist+getStateDepth();
      }
      else
      { return -1;
      }
    }
    else
    { return -1;
    }
  }

}
