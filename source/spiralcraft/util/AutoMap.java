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

/**
 * A Map which uses a KeyFunction to automatically generate Keys for
 *   inserted values.
 */
public class AutoMap<K,V>
  extends MapWrapper<K,V>
{
  private final KeyFunction<K,V> _keyFunction;
  
  public AutoMap(Map<K,V> impl,KeyFunction<K,V> function)
  { 
    super(impl);
    _keyFunction=function;
  }
  
  public void put(V value)
  { put(_keyFunction.key(value),value);
  }
  
  public void removeValue(V value)
  { remove(_keyFunction.key(value));
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public boolean containsValue(Object value)
  { 
    // Can't use V as param b/c of Map interface
    return containsKey(_keyFunction.key((V) value));
  }
}
