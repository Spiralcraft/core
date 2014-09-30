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

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;

/**
 * <p>A Transition is responsible for changing the current state of the 
 *   StateMachine. It is one of potentially many associated with a particular
 *   State. 
 * </p>
 * 
 * <p>A Transition can have conditions and behavior associated with its
 *   invocation.
 * </p>
 * 
 * It can trigger some behavior and 
 * 
 * @author mike
 *
 */
public class Transition
  implements Contextual
{

  private String name;
  private String nextStateName;
  private State nextState;
  private StateMachine fsm;
  
  public void setName(String name)
  { this.name=name;
  }
  
  public String getName()
  { return name;
  }
  
  /**
   * <p>The name of the next state to transition the StateMachine to when this
   *   Transition is invoked.
   * </p>
   *   
   * @param name
   */
  public void setNext(String nextStateName)
  { this.nextStateName=nextStateName;
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  {
    fsm=LangUtil.findInstance(StateMachine.class,focusChain);
    nextState=fsm.getState(nextStateName);
    
    return focusChain;
  }
  
  /**
   * Invoke this transition. Returns true if successfully invoked, or false
   *   if a condition was not met. 
   * 
   * @return
   */
  boolean invoke()
  { 
    fsm.completeTransition(nextState);
    return true;
  }

}
