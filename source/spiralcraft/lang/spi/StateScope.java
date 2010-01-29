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
package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;

/**
 * <p>Referenced by upstream channels that wish to store state for the duration 
 *   of an arbitrary client-defined transaction
 * </p>
 * 
 * @author mike
 */
public class StateScope
  implements FocusChainObject
{

  @SuppressWarnings("unchecked")
  private final ThreadLocalChannel<State[]> stateChannel
    =new ThreadLocalChannel<State[]>
    (BeanReflector.<State[]>getInstance(State[].class));
  private int dataLen;
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    focusChain
      =focusChain.chain(new SimpleChannel<StateScope>(this,true));
    
    return focusChain;
  }

  public void push()
  { stateChannel.push(new State<?>[dataLen]);
  }
  
  public void set(State<?>[] states)
  { stateChannel.set(states);
  }
  
  public State<?>[] get()
  { return stateChannel.get();
  }
  
  public void pop()
  { stateChannel.pop();
  }
  
  public <X> Channel<State<X>> bind(Reflector<X> dataReflector)
  {
    GenericReflector<State<X>> stateReflector
      =new GenericReflector<State<X>>
        (BeanReflector.<State<X>>getInstance(State.class));
    stateReflector.enhance("data",dataReflector);
    return new StateChannel<X>(stateReflector);
  }

  public class State<T>
  { 
    public boolean frameChanged;
    public T data;
        
  }
  
  public class StateChannel<T>
    extends AbstractChannel<State<T>>
  {
    private final int index=dataLen++;
    
    public StateChannel(Reflector<State<T>> stateReflector)
    { super(stateReflector);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected State<T> retrieve()
    { 
      State<?>[] states=stateChannel.get();
      if (states!=null)
      { return (State<T>) states[index];
      }
      else
      { return null;
      }
    }

    @Override
    protected boolean store(
      State<T> val)
      throws AccessException
    { 
      State<?>[] states=stateChannel.get();
      if (states!=null)
      { 
        states[index]=val;
        return true;
      }
      return false;
    }
    
  }
    
}
