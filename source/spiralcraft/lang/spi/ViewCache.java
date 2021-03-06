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

import java.net.URI;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.util.LangUtil;

/**
 * <p>Referenced by upstream channels that wish to store state for the duration 
 *   of an arbitrary client-defined transaction
 * </p>
 * 
 * @author mike
 */
public class ViewCache
  implements Contextual
{
  public static final URI FOCUS_URI
    =URI.create("class:/spiralcraft/lang/spi/ViewCache");
  
  public static final ViewCache find(Focus<?> focus)
  {
    Focus<ViewCache> stateScopeFocus=focus.<ViewCache>findFocus(FOCUS_URI);
    if (stateScopeFocus==null)
    { return null;
    }
    else
    { return stateScopeFocus.getSubject().get();
    }
  }  

  
  @SuppressWarnings("rawtypes")
  private final ThreadLocalChannel<ViewState[]> compVectorChannel
    =new ThreadLocalChannel<ViewState[]>
    (BeanReflector.<ViewState[]>getInstance(ViewState[].class));
  private volatile int dataLen;

  public ViewCache(Focus<?> focus)
  { compVectorChannel.setContext(focus);
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    focusChain
      =focusChain.chain(LangUtil.constantChannel(this));
    
    return focusChain;
  }

  public int getSize()
  { return dataLen;
  }
  
  public void setSize(int size)
  { dataLen=size;
  }
  
  
  
  public void push()
  { compVectorChannel.push(null);
  }
  
  /**
   * Initialize the set of states for storing running elements of the  
   *   computation sequence currently referenced by the stateChannel
   */
  public void init()
  {
    if (dataLen==0)
    { return;
    }

    ViewState<?>[] states
      =new ViewState<?>[dataLen];
    for (int i=0;i<states.length;i++)
    { states[i]=new ViewState<Object>();
    }
    compVectorChannel.set(states);
  }
  
  public void touch()
  {
    if (dataLen==0)
    { return;
    }

    ViewState<?>[] states=compVectorChannel.get();
    for (int i=0;i<states.length;i++)
    { states[i].frameChanged=true;
    }    
  }
  
  public void checkpoint()
  {
    if (dataLen==0)
    { return;
    }

    ViewState<?>[] states=compVectorChannel.get();
    for (int i=0;i<states.length;i++)
    { states[i].checkpoint=true;
    }    
  }
  
  public void set(ViewState<?>[] states)
  { compVectorChannel.set(states);
  }
  
  public ViewState<?>[] get()
  { return compVectorChannel.get();
  }
  
  public void pop()
  { compVectorChannel.pop();
  }
  
  public <X> Channel<ViewState<X>> bind(Reflector<X> dataReflector)
  {
    GenericReflector<ViewState<X>> stateReflector
      =new GenericReflector<ViewState<X>>
        (BeanReflector.<ViewState<X>>getInstance(ViewState.class));
    stateReflector.enhance("data",dataReflector);
    return new StateChannel<X>(stateReflector);
  }

  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public class StateChannel<T>
    extends SourcedChannel<ViewState[],ViewState<T>>
  {
    private final int index=dataLen++;
    
    public StateChannel(Reflector<ViewState<T>> stateReflector)
    { super(stateReflector,compVectorChannel);
    }

    @Override
    protected ViewState<T> retrieve()
    { 
      ViewState<?>[] states=source.get();
      if (states!=null)
      { return (ViewState<T>) states[index];
      }
      else
      { return null;
      }
    }

    @Override
    protected boolean store(
      ViewState<T> val)
      throws AccessException
    { 
      ViewState<?>[] states=source.get();
      if (states!=null)
      { 
        states[index]=val;
        return true;
      }
      return false;
    }
    
  }
    
}
