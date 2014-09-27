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

import java.lang.reflect.Array;

import spiralcraft.app.Component;
import spiralcraft.app.DispatchFilter;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.kit.AbstractController;
import spiralcraft.app.kit.FrameHandler;
import spiralcraft.app.kit.ValueState;
import spiralcraft.app.kit.StandardContainer;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.util.string.StringConverter;

/**
 * Selects from a set of Cases or other components by matching a runtime value
 *   against the constant values of several cases. If a Case component is not
 *   used, the constant value will be the String id of the child component.
 * 
 * @author mike
 *
 */
public class Switch<T>
  extends AbstractController<ValueState<T>>
{

  private Binding<T> x;
  private T[] constants;
  
  public void setX(Binding<T> x)
  { 
    removeParentContextual(this.x);
    this.x=x;
    addParentContextual(this.x);
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public Class<SwitchState> getStateClass()
  { return SwitchState.class;
  }
  
  @Override
  public SwitchState<T> getState()
  { return (SwitchState<T>) super.getState();
  }
  
  @Override
  public SwitchState<T> createState()
  { return new SwitchState<T>(getChildCount(),null);
  }
  
  @Override
  protected void addHandlers()
  {
    super.addHandlers();
    addHandler(new SwitchHandler());
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected void bindComplete(Focus<?> focus)
    throws ContextualException
  {
    Component[] children=getChildren();
    
    constants=(T[]) Array.newInstance(x.getContentType(),children.length);
    StringConverter<T> converter=x.getReflector().getStringConverter();
    for (int i=0;i<children.length;i++)
    {
      if (children[i] instanceof Case)
      { constants[i]=((Case<T>) children[i]).getConstant();
      }
      else
      { 
        
        if (converter!=null)
        { constants[i]=converter.fromString(children[i].getId());
        }
        else
        { constants[i]=(T) children[i].getId();
        }
      }
    }
    
    ((StandardContainer ) childContainer).setDispatchFilter(new SwitchFilter());
    super.bindComplete(focus);
  }
  
  class SwitchFilter
    implements DispatchFilter
  {
    @Override
    public boolean[] childMask(
      Dispatcher context,
      Message message)
    {return getState().mask;
    }
  }
  
  class SwitchHandler
    extends FrameHandler
  {

    @Override
    protected void doHandler(
      Dispatcher dispatcher,
      Message message,
      MessageHandlerChain next)
    {
      T val=x.get();
      SwitchState<T> state=getState();
     
      if (state.getValue()!=val
          && (val==null
             || !val.equals(state.getValue())
             )
          )
      {
        state.setValue(val);
        for (int i=0;i<constants.length;i++)
        { 
          log.fine(""+constants[i]);
          state.mask[i]=constants[i]!=null && constants[i].equals(val);
        }
      }
      
      next.handleMessage(dispatcher,message);
    }
  }
}

class SwitchState<T>
  extends ValueState<T>
{
  boolean[] mask;

  public SwitchState(
    int childCount,
    String id)
  { 
    super(childCount, id);
    mask=new boolean[childCount];
  }
  
}
