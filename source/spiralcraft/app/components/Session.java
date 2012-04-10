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

import spiralcraft.app.State;
import spiralcraft.app.kit.AbstractModelComponent;
import spiralcraft.app.kit.ValueState;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

/**
 * <p>Publishes the result an Expression into the application model
 * </p>
 * 
 * @author mike
 *
 */
public class Session<T>
  extends AbstractModelComponent<T>
{

  private Binding<T> x;
  private Binding<?> onStart;
  
  public void setX(Binding<T> x)
  {
    this.removeParentContextual(this.x);
    this.x=x;
    this.addParentContextual(this.x);
  }
  
  public void setOnStart(Binding<?> onStart)
  {
    this.removeExportContextual(this.onStart);
    this.onStart=onStart;
    this.addExportContextual(this.onStart);
  }
  
  @Override
  protected Class<? extends State> getStateClass()
  { return SessionState.class;
  }
  
  @Override
  protected Channel<T> bindSource(
    Focus<?> focusChain)
    throws BindException
  { return x;
  }
  
  @Override
  protected T compute(ValueState<T> state)
  {
    SessionState<T> sessionState=(SessionState<T>) state;
    if (!sessionState.seen)
    { 
      T value=x.get();
      if (onStart!=null)
      {
        // XXX Move to a handler b/c value isn't visible
        onStart.get();
      }
      sessionState.seen=true;
      return value;
    }
    else
    { return sessionState.getValue();
    }
  }

}

class SessionState<T>
  extends ValueState<T>
{
  volatile boolean seen;
  
  public SessionState(int childCount,String id)
  { super(childCount, id);
  }
}
