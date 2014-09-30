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
import java.util.HashMap;

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
 * <p>Selects from a set of Cases or other components by matching a runtime value
 *   against the constant values of several cases. If a Case component is not
 *   used, the constant value will be the String id of the child component.
 * </p>
 * 
 * <p>Only selected components will receive messages. The new selection takes
 *   effect on frame change.
 * </p>
 * 
 * @author mike
 *
 */
public class Switch<T>
  extends AbstractController<ValueState<T>>
{

  private Binding<T> x;
  private T[] constants;
  private boolean[] emptyMask;
  private final HashMap<T,boolean[]> maskMap=new HashMap<>();
  
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
  { 
    SwitchState<T> state= new SwitchState<T>(getChildCount(),null);
    state.mask=emptyMask;
    return state;
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
    emptyMask=new boolean[children.length];
    
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
      
      T constant=constants[i];
      
      boolean[] mask=maskMap.get(constant);
      if (mask==null)
      { 
        mask=new boolean[children.length];
        maskMap.put(constant,mask);
      }
      mask[i]=true;
      
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
    { return getState().mask;
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
        boolean[] mask=maskMap.get(val);
        if (mask==null)
        { mask=emptyMask;
        }
        state.setMask(mask);
      }
      
      if (state.maskChanged)
      {
        state.maskChanged=false;
        //TODO: Send an Activate event to children so the can refresh
      }
        
        
      next.handleMessage(dispatcher,message);
    }
  }
}

class SwitchState<T>
  extends ValueState<T>
{
  boolean[] mask;
  
  boolean maskChanged;

  public SwitchState(
    int childCount,
    String id)
  { 
    super(childCount, id);
  }
  
  void setMask(boolean[] mask)
  { 
    if (this.mask!=mask)
    { 
      maskChanged=true;
      this.mask=mask;
    }
  }
  
}
