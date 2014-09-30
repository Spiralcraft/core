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
  
  public void setStates(State[] states)
  { this.states=states;
  }
  
  public State getState(String stateName)
  { return stateMap.get(stateName);
  }
  
  public State currentState()
  { return currentState.get();
  }
  
  public State getInitialState()
  { return states[0];
  }
  
  void completeTransition(State nextState)
  { currentState.set(nextState);
  }
  
  public void setStateX(Binding<State> stateX)
  { currentState=stateX;
  }
  
  public void transition(String transitionName)
  { currentState.get().transition(transitionName);
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
    for (State s:states)
    { s.bind(focusChain);
    }
    return focusChain;
  }

}
