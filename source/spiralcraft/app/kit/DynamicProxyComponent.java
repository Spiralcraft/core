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

import spiralcraft.app.Component;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.DisposeMessage;
import spiralcraft.app.InitializeMessage;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.State;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.log.Level;

/**
 * Dynamically loads a child component that with a lifecycle tied to the
 *   state of this component.
 * 
 * @author mike
 *
 */
public class DynamicProxyComponent
  extends AbstractComponent
{

  class Handler
    extends AbstractMessageHandler
  {

    @Override
    protected void doHandler(
      Dispatcher dispatcher,
      Message message,
      MessageHandlerChain next)
    {
      DynamicProxyState state=(DynamicProxyState) dispatcher.getState();
      if (state.component==null)
      {
        Component component=createComponent(dispatcher);
        
        if (component!=null)
        {
          component.setParent(DynamicProxyComponent.this);
          try
          { 
            component.bind(selfFocus);
            component.start();
            state.component=component;
          }
          catch (ContextualException x)
          { log.log(Level.SEVERE,"Error binding component "+component,x);
          }
          catch (LifecycleException x)
          { log.log(Level.SEVERE,"Error starting component "+component,x);
          }
          
        }
      }
      if (state.component!=null)
      {
       
        if (state.componentState==null)
        { 
          state.componentState=state.component.createState();
          state.componentState.link(state.getParent(),state.getPath());
          
          if (message.getType()!=InitializeMessage.TYPE)
          { 
            dispatcher.dispatch
              (InitializeMessage.INSTANCE
              ,state.component
              ,state.componentState
              ,null
              );
          }
        }
        
        dispatcher.dispatch
          (message,state.component,state.componentState,dispatcher.getForwardPath());
        
        if (message.getType()==DisposeMessage.TYPE)
        {
          try
          { state.component.stop();
          }
          catch (LifecycleException x)
          { log.log(Level.SEVERE,"Error stopping component "+state.component,x);
          }
          state.component=null;
        }
      }
      else
      { log.warning("Failed to create component, ignoring message "+message);
      }
    }
  }

  protected Component createComponent(Dispatcher dispatcher)
  { return null;
  }
  
  @Override
  protected void addHandlers()
  { 
    super.addHandlers();
    addHandler(new Handler());
  }
  
  @Override
  protected Class<? extends State> getStateClass()
  { return DynamicProxyState.class;
  }
}

class DynamicProxyState
  extends SimpleState
{

  Component component;
  State componentState;
  
  public DynamicProxyState(int childCount,String id)
  { super(childCount, id);
  }
  
}