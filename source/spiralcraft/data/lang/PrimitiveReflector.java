//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.data.lang;

import spiralcraft.lang.Extender;
import spiralcraft.lang.reflect.BeanReflector;

import spiralcraft.data.Type;

public class PrimitiveReflector<T>
  extends Extender<T>
{

  private final Type<T> type;
  
  public PrimitiveReflector(Type<T> type)
  { 
    super(BeanReflector.<T>getInstance(type.getNativeClass()));
    this.type=type;
  }
  
  public Type<T> getType()
  { return type;
  }

}
