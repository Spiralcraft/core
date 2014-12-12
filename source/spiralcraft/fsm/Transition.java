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
import spiralcraft.common.declare.Declarable;
import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;
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
  implements Contextual,Declarable
{

  private String name;
  private String nextStateName;
  private State nextState;
  private StateMachine fsm;
  private Binding<?> onInvoke;
  private Binding<?> onComplete;
  private Binding<Boolean> when;
  private Focus<Transition> self;
  private DeclarationInfo declarationInfo;
  
  public void setName(String name)
  { this.name=name;
  }
  
  public String getName()
  { return name;
  }

  
  /**
   * A condition that must evaluate to true in order for the Transition to
   *   be invoked.
   *   
   * @param when
   */
  public void setWhen(Binding<Boolean> when)
  { this.when=when;
  }

  /**
   * Triggered when the transition is successfully invoked.
   *   
   * @param when
   */
  public void setOnInvoke(Binding<?> onInvoke)
  { this.onInvoke=onInvoke;
  }

  /**
   * Triggered when the transition is completed, after any state change has
   *   taken place.
   *   
   * @param onComplete
   */
  public void setOnComplete(Binding<?> onComplete)
  { this.onComplete=onComplete;
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
  public void setDeclarationInfo(DeclarationInfo declarationInfo)
  { this.declarationInfo=declarationInfo;
  }
  
  @Override
  public DeclarationInfo getDeclarationInfo()
  { return declarationInfo;
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  {
    focusChain=focusChain.chain(focusChain.getSubject());
    self=focusChain.chain(new SimpleChannel<Transition>(this,true));
    focusChain.addFacet(self);
    
    fsm=LangUtil.findInstance(StateMachine.class,focusChain);
    State thisState=LangUtil.findInstance(State.class,focusChain);
    nextState=(nextStateName!=null?fsm.getState(nextStateName):thisState);
    if (nextState==null)
    { 
      throw new BindException
        ("Unknown state '"+nextStateName+"' in transition '"+name+"'"
        ,getDeclarationInfo()
        ,null
        );
    }
    if (when!=null)
    { 
      when.bind(focusChain);
      when.assertContentType(Boolean.class);
    }
    if (onInvoke!=null)
    { onInvoke.bind(focusChain);
    }
    if (onComplete!=null)
    { onComplete.bind(focusChain);
    }
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
    if (when!=null && !Boolean.TRUE.equals(when.get()))
    { return false;
    }
    if (onInvoke!=null)
    { onInvoke.get();
    }
    fsm.completeTransition(nextState);
    if (onComplete!=null)
    { onComplete.get();
    }
    return true;
  }

}
