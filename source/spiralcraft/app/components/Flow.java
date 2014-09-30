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
package spiralcraft.app.components;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.InitializeMessage;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.State;
import spiralcraft.app.kit.AbstractController;
import spiralcraft.app.kit.AbstractMessageHandler;
import spiralcraft.app.kit.ValueState;
import spiralcraft.common.ContextualException;
import spiralcraft.fsm.StateMachine;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>A controller which responds to events and triggers behavior based on
 *   a state machine.
 * </p>
 * 
 * @author mike
 *
 */
public class Flow<T>
  extends AbstractController<FlowState<T>>
{
  private final StateMachine fsm=new StateMachine();
  protected ThreadLocalChannel<T> modelChannel;

  private Binding<T> model;
  private Binding<?> onStart;
  
  public void setModel(Binding<T> model)
  {
    this.removeParentContextual(this.model);
    this.model=model;
    this.addParentContextual(this.model);
  }
  
  public void setOnStart(Binding<?> onStart)
  {
    this.removeExportContextual(this.onStart);
    this.onStart=onStart;
    this.addExportContextual(this.onStart);
  }
  
  
  public void transition(String transitionName)
  { fsm.transition(transitionName);
  }
  
  public void setStates(spiralcraft.fsm.State[] states)
  { fsm.setStates(states);
  }
  
  public StateMachine getStateMachine()
  { return fsm;
  }
  
  @Override
  protected Class<? extends State> getStateClass()
  { return FlowState.class;
  }
  
  @Override
  public FlowState<T> createState()
  { 
    FlowState<T> state=new FlowState<T>(getChildCount(),null);
    state.currentState=fsm.getInitialState();
    return state;
  }
  
  @Override
  public FlowState<T> getState()
  { return super.getState();
  }
  
  public spiralcraft.fsm.State getFlowState()
  { return getState().getCurrentState();
  }
  
  public void setFlowState(spiralcraft.fsm.State state)
  { getState().setCurrentState(state);
  }

  @Override
  protected void addHandlers()
  {
    super.addHandlers();
    addHandler(new ModelInitHandler());
    addHandler(new ModelHandler());
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> chain)
    throws ContextualException
  {
    modelChannel=new ThreadLocalChannel<T>(model,true);
    chain=chain.chain(modelChannel);
    try
    {
      fsm.setStateX
        (new Binding<spiralcraft.fsm.State>
           (Expression.<spiralcraft.fsm.State>parse
             ("[:class:/spiralcraft/app/components/Flow].flowState")
           )
        );
    }
    catch (ParseException x)
    { 
      throw new ContextualException
        ("Internal error binding FSM"
        ,getDeclarationInfo()
        ,x
        );
    }
    fsm.bind(chain);
    
    return chain;
  }

  private void onInit()
  { 
    getState().setValue(model.get());
    if (logLevel.isFine())
    { log.fine("Initialized model "+getState().getValue());
    }
  }
  
  
  class ModelInitHandler
    extends AbstractMessageHandler
  {
    { type=InitializeMessage.TYPE;
    }
  
    @Override
    protected void doHandler(
      Dispatcher dispatcher,
      Message message,
      MessageHandlerChain next)
    {
      onInit();
      next.handleMessage(dispatcher,message);
    }
  }
 
  class ModelHandler
    extends AbstractMessageHandler
  {
    @Override
    protected void doHandler(
      Dispatcher dispatcher,
      Message message,
      MessageHandlerChain next)
    {
      modelChannel.push(getState().getValue());
      next.handleMessage(dispatcher,message);
      modelChannel.pop();
    }

  }
}


class FlowState<T>
  extends ValueState<T>
{
  spiralcraft.fsm.State currentState;
  
  public FlowState(
    int childCount,
    String id)
  { super(childCount, id);
  }

  public spiralcraft.fsm.State getCurrentState()
  { return currentState;
  }
  
  public void setCurrentState(spiralcraft.fsm.State currentState)
  { this.currentState=currentState;
  }
}
