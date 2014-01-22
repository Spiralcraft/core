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


import spiralcraft.app.kit.AbstractModelComponent;
import spiralcraft.app.kit.ValueState;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

/**
 * Dynamically provides a view based on the actual data type of the model
 *   value.
 * 
 * @author mike
 *
 */
public class Polyview<T>
  extends AbstractModelComponent<T>
{

  private Binding<T> x;
  
  public void setX(Binding<T> x)
  { this.x=x;
  }

  @Override
  protected Channel<T> bindSource(
    Focus<?> focusChain)
    throws BindException
  { 
    x.bind(focusChain);
    return x;
  }

  @Override
  protected T compute(
    ValueState<T> state)
  { return state.getValue();
  }
  
  @Override
  protected void onCompute(ValueState<T> state,T oldValue,T newValue)
  {
    log.fine
      ("Subtype is "
        +channel.getReflector().subtype(channel.get()).getTypeURI()
      );
    
    
  }
  
}
