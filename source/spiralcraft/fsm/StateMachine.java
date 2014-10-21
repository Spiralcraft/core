//
//Copyright (c) 2014 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.fsm;

import java.util.HashMap;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * <p>Responds to and controls application behaviors by using states and
 *   transitions.
 * </p>
 * 
 * <p>A StateMachine is a Contextual object, so its internal state is tied to
 *   the context in which it is used.
 * </p>
 * 
 * @author mike
 *
 */
public class StateMachine
  implements Contextual
{

  private State[] states;
  private Focus<StateMachine> self;
  private final HashMap<String,State> stateMap=new HashMap<>();
  private Binding<State> currentState;
  private Binding<?> beforeStateChange;
  private Binding<?> afterStateChange;
  private Binding<?> beforeTransition;
  private Binding<?> afterTransition;
  private Binding<String> initialStateX;
  
  public void setStates(State[] states)
  { this.states=states;
  }
  
  public State getState(String stateName)
  { return stateMap.get(stateName);
  }
  
  public State currentState()
  { return currentState.get();
  }
  
  public void restart()
  { changeState(getInitialState());
  }
  
  /**
   * Resume execution at a State recorded externally.
   * 
   * @param stateName
   */
  public void resume(String stateName)
  { 
    State state=stateMap.get(stateName);
    if (state==null)
    { throw new IllegalArgumentException("No state named '"+state+"'");
    }
    changeState(state);
  }
  
  public State getInitialState()
  { 
    State initialState=null;
    if (initialStateX!=null)
    { 
      String stateName=initialStateX.get();
      initialState=this.stateMap.get(stateName);
    }
    
    if (initialState==null)
    { initialState=states[0];
    }
    return initialState;
  }
  
  public void setInitialStateX(Binding<String> initialStateX)
  { this.initialStateX=initialStateX;
  }
  
  public void setStateX(Binding<State> stateX)
  { currentState=stateX;
  }
  
  public void transition(String transitionName)
  { 
    if (beforeTransition!=null)
    { beforeTransition.get();
    }
    if (currentState.get().transition(transitionName))
    { 
      if (afterTransition!=null)
      { afterTransition.get();
      }
    }
    
  }
  
  /**
   * Triggered when the current state is still active before changing to a
   *   new, different state
   * @param beforeStateChange
   */
  public void setBeforeStateChange(Binding<?> beforeStateChange)
  { this.beforeStateChange=beforeStateChange;
  }

  /**
   * Triggered whenever new, different state becomes active
   * @param beforeStateChange
   */
  public void setAfterStateChange(Binding<?> afterStateChange)
  { this.afterStateChange=afterStateChange;
  }
  
  /**
   * Triggered before a transition is attempted, regardless of the outcome
   * 
   * @param beforeTransition
   */
  public void setBeforeTransition(Binding<?> beforeTransition)
  { this.beforeTransition=beforeTransition;
  }
  
  /**
   * Triggered after a successful transition
   * 
   * @param beforeTransition
   */
  public void setAfterTransition(Binding<?> afterTransition)
  { this.afterTransition=afterTransition;
  }

  void completeTransition(State nextState)
  { changeState(nextState);
  }

  private void changeState(State newState)
  {
    State state=currentState.get();
    if (state!=newState)
    {
      if (beforeStateChange!=null)
      { beforeStateChange.get();
      }
      state.onExit();
      currentState.set(newState);
      newState.onEnter();
      if (afterStateChange!=null)
      { afterStateChange.get();
      }
    }
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes"})
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  {
    if (states==null || states.length==0)
    { 
      states=new State[] {new State()};
      states[0].setName("default");
    }
    
    focusChain=focusChain.chain(focusChain.getSubject());
    for (State s:states)
    { stateMap.put(s.getName(),s);
    }
    
    if (currentState!=null)
    { currentState.bind(focusChain);
    }
    else
    { currentState=new Binding(focusChain.getSubject());
    }
    currentState.assertContentType(State.class);
    
    self=focusChain.chain(new SimpleChannel<StateMachine>(this,true));
    focusChain.addFacet(self);
    if (beforeStateChange!=null)
    { beforeStateChange.bind(focusChain);
    }
    if (afterStateChange!=null)
    { afterStateChange.bind(focusChain);
    }
    if (beforeTransition!=null)
    { beforeTransition.bind(focusChain);
    }
    if (afterTransition!=null)
    { afterTransition.bind(focusChain);
    }
    if (initialStateX!=null)
    { initialStateX.bind(focusChain);
    }
    for (State s:states)
    { s.bind(focusChain);
    }
    return focusChain;
  }

}
