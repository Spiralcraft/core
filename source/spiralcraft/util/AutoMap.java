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
public class AutoMap
  extends MapWrapper
{
  private final KeyFunction _keyFunction;
  
  public AutoMap(Map impl,KeyFunction function)
  { 
    super(impl);
    _keyFunction=function;
  }
  
  public void put(Object value)
  { put(_keyFunction.key(value),value);
  }
  
  public void removeValue(Object value)
  { remove(_keyFunction.key(value));
  }
  
  public boolean containsValue(Object value)
  { return containsKey(_keyFunction.key(value));
  }
}
