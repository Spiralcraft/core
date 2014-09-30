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
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * An active State in a StateMachine. References a set of valid transitions 
 *   that can be applied while in this state.
 * 
 * @author mike
 *
 */
public class State
  implements Contextual
{

  private String name;
  private Transition[] transitions;
  private Focus<State> self;
  private HashMap<String,Transition> transitionMap=new HashMap<>();
  
  public void setName(String name)
  { this.name=name;
  }
  
  public String getName()
  { return name;
  }
  
  public void setTransitions(Transition[] transitions)
  { 
    this.transitions=transitions;
    transitionMap.clear();
    for (Transition transition:transitions)
    { 
      if (transition.getName()!=null 
          && transitionMap.containsKey(transition.getName())
          )
      {
        throw new IllegalArgumentException
          ("Duplicate transition name "+transition.getName());
      }
      transitionMap.put(transition.getName(),transition);
    }
  }
  
  /**
   * Invokes the first valid transition
   */
  public void transition()
  {
    for (Transition transition: transitions)
    { 
      if (transition.invoke())
      { return;
      }
    }
    
  }
  
  /**
   * Attempts to invokes the transition with the specified name
   * 
   * @param transitionName
   */
  public void transition(String transitionName)
  { 
    Transition transition=transitionMap.get(transitionName);
    if (transition==null)
    { 
      throw new IllegalArgumentException
        ("No transition named '"+transitionName+"'");
    }
    transition.invoke();
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  {
    focusChain=focusChain.chain(focusChain.getSubject());
    self=focusChain.chain(new SimpleChannel<State>(this,true));
    focusChain.addFacet(self);
    for (Transition t: transitions)
    { t.bind(focusChain);
    }
    return focusChain;
  }

}
