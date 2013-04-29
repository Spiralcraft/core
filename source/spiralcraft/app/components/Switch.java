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
import spiralcraft.app.kit.AbstractController;
import spiralcraft.app.kit.ValueState;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.util.string.StringConverter;

/**
 * Selects from a set of Cases by matching a runtime value against the
 *   bind-time values of several cases 
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
  public Class<ValueState> getStateClass()
  { return ValueState.class;
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
    super.bindComplete(focus);
  }
}
