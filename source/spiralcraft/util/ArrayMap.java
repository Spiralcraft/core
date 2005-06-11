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
 * A MultiMap which uses Arrays to store collections of values for a key
 */
public class ArrayMap
  extends MapWrapper
  implements MultiMap
{

  private final Class _arrayComponentClass;

  public ArrayMap(Map map,Class arrayComponentClass)
  { 
    super(map);
    _arrayComponentClass=arrayComponentClass;
  }

  /**
   * Associates a key with single element array containing the
   *   specified value
   */
  public void set(Object key,Object value)
  { map.put(key,ArrayUtil.newInstance(_arrayComponentClass,value));
  } 

  /**
   * Append the value to the array indexed to the specified key.
   * If the array does not exist, it will be created
   */
  public void add(Object key,Object value)
  { 
    Object array=map.get(key);
    if (array==null)
    { set(key,value);
    }
    else
    { map.put(key,ArrayUtil.append(array,value));
    }
  }

  public void remove(Object key,Object value)
  {
    Object array=map.get(key);
    if (array!=null)
    { map.put(key,ArrayUtil.remove(array,value));
    }
  }
  
  /**
   * Return the first element of the array mapped to the specified key
   */
  public Object getOne(Object key)
  { return ArrayUtil.getFirstElement(map.get(key));
  }

}
