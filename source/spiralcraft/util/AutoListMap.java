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
package spiralcraft.util;

import java.util.Map;
import java.util.List;

/**
 * A ListMap which uses a KeyFunction to automatically generate Keys for
 *   inserted values.
 */
public class AutoListMap<K,T>
  extends ListMap<K,T>
{
  private final KeyFunction<K,T> _keyFunction;
  
  public AutoListMap(Map impl,KeyFunction function)
  { 
    super(impl);
    _keyFunction=function;
  }
  
  public void setValue(T value)
  { set(_keyFunction.key(value),value);
  }
  
  public void removeValue(T value)
  { remove(_keyFunction.key(value),value);
  }
  
  public void addValue(T value)
  { add(_keyFunction.key(value),value);
  }
  
  public boolean containsValue(Object value)
  { 
      
    List<T> list=get(_keyFunction.key((T) value));
    if (list==null)
    { return false;
    }
    return list.contains(value);
      
  }
}
