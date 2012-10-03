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

import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;

public class Case<T>
  extends AbstractComponent
{

  private Binding<T> constantX;
  private T constant;
  
  void setConstantX(Binding<T> constantX)
  { 
    removeParentContextual(this.constantX);
    this.constantX=constantX;
    addParentContextual(this.constantX);
  }
  
  @Override
  public void bindComplete(Focus<?> focus)
  {
    constant=constantX.get();
  }
  
  public T getConstant()
  { return constant;
  }
}
