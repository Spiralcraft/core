//
// Copyright (c) 2008 Michael Toth
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
package spiralcraft.data.util;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.util.string.StringConverter;

public class TypeStringConverter<T>
  extends StringConverter<T>
{

  private Type<T> type;
  
  public TypeStringConverter(Type<T> type)
  { this.type=type;
  }
  
  @Override
  public T fromString(
    String val)
  {
    try
    { return type.fromString(val);
    }
    catch (DataException x)
    { throw new IllegalArgumentException(val,x);
    }
  }
  
  @Override
  public String toString(T val)
  { return type.toString(val);
  }

}
