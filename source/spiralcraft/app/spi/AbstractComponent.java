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
import spiralcraft.app.Event;
import spiralcraft.app.Parent;
import spiralcraft.app.Message;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.State;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;
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
  implements Component,Parent
{
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level logLevel=ClassLog.getInitialDebugLevel(getClass(),null);
  protected Level normalLogLevel=logLevel;
  
  protected final MessageHandlerSupport handlers
    =new MessageHandlerSupport();
  
  protected Parent parent;
  protected boolean bound=false;
  protected Container childContainer;
  protected Focus<?> selfFocus;
  protected boolean acceptsChildren=true;

  protected boolean exportSelf=true;  
  protected String id;
  protected Object key;
  
  public void setParent(Parent parent)
  { this.parent=parent;
  }
  
  @Override
  public Parent getParent()
  { return parent;
  }
  
  /**
   * A public identifier by which this component can be addressed within its
   *   parent component. Must be unique within the parent component.
   * 
   */
  public void setId(String id)
  { this.id=id;
  }
  
  /**
   * A public identifier by which this component can be addressed within its
   *   parent component.
   * 
   * @return
   */
  public String getId()
  { return this.id;
  }

//  /**
//   * An non-unique value that associates this component with some aspect of
//   *   its parent container.
//   * 
//   */
//  @Override
//  public void setKey(Object key)
//  { this.key=key;
//  }
//  
//  /**
//   * A non-unique value that associates this component with some aspect of
//   *   its parent container.
//   * 
//   * @return
//   */
//  @Override
//  public Object getKey()
//  { return this.key;
//  }

  
  
  
  @Override
  public void message
    (Dispatcher context
    ,Message message
    )
  { 
    // Calls the context when done
    handlers.getChain(message.getType())
      .handleMessage(context,message);
  }

  @Override
  public Container asContainer()
  { return childContainer;
  }

  @Override
  public Parent asParent()
  { return this;
  }
  
  /**
   * <p>Override to create a new State.
   */
  @Override
  public State createState(State parentState)
  { return new SimpleState(-1,parentState,id);
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    Focus<?> context=focusChain;
    if (selfFocus==null)
    { 
      selfFocus=focusChain.chain
        (new SimpleChannel<AbstractComponent>(this,true));
    }

    focusChain=bindImports(focusChain);
    focusChain=handlers.bind(focusChain);
    
    focusChain=bindExports(focusChain);
    if (exportSelf)
    {
      if (focusChain==context)
      { focusChain=selfFocus;
      }
      else
      { focusChain.addFacet(selfFocus);
      }
    }
    if (childContainer!=null)
    { childContainer.bind(focusChain);
    }
    return focusChain;
  }

  @Override
  public void start()
    throws LifecycleException
  { 
    handlers.start();
    if (childContainer!=null)
    { childContainer.start();
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    if (childContainer!=null)
    { childContainer.stop();
    }
    handlers.stop();
  }

  protected Focus<?> bindImports(Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }

  protected Focus<?> bindExports(Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }
  
  @Override
  public int getStateDepth()
  { return 1;
  }

  protected State getState(Dispatcher context)
  { return context.getState();
  }
  
  @Override
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

  @Override
  public Component asComponent()
  { return this;
  }

  public void setChildren(final Component[] newChildren)
  {
    if (!acceptsChildren)
    { 
      throw new UnsupportedOperationException
        (getClass()+" does not accept children");
    }
    Component[] children=new Component[newChildren.length];
    System.arraycopy(newChildren,0,children,0,newChildren.length);
    
    this.childContainer
      =new StandardContainer(children);
  }
    
  @Override
  public void handleEvent(
    Dispatcher context,
    Event event)
  { context.handleEvent(event);    
  }

  @Override
  public void setLogLevel(Level logLevel)
  { 
    this.logLevel=logLevel;
    this.normalLogLevel=logLevel;
  }

  public void setDebug(boolean debug)
  { 
    if (debug)
    { this.logLevel=Level.FINE;
    }
    else
    { 
      if (this.logLevel.canLog(Level.DEBUG))
      { this.logLevel=this.normalLogLevel;
      }
    }
  }
}
