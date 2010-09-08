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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides an interface to a Map which maps keys to Lists for the purpose
 *   of associating multiple values with a single key
 */
public class ListMap<K,T>
  extends MapWrapper<K,List<T>>
  implements MultiMap<K,T>
{

  public ListMap()
  { super(new LinkedHashMap<K,List<T>>());
  }
  
  public ListMap(Map<K,List<T>> map)
  { super(map);
  }

  /**
   * <P>Associate a key with single element List containing the
   *   specified value
   * </P>
   */
  @Override
  public void set(K key,T value)
  { 
    List<T> list=new LinkedList<T>();
    list.add(value);
    put(key,list);
  } 

  /**
   * <P>Append the value to the List indexed to the specified key.
   *   If the List does not exist, it will be created
   * </P>
   */
  @Override
  public void add(K key,T value)
  { 
    List<T> list=get(key);
    if (list==null)
    { set(key,value);
    }
    else
    { list.add(value);
    }
  }

  /**
   * <P>Remove the value from the List indexed to the specified key. If the 
   *   List is empty after removal, the key will be removed.
   * </P>
   */
  @Override
  public void remove(K key,T value)
  { 
    List<T> list=get(key);
    if (list!=null)
    { list.remove(value);
    }
    if (list.isEmpty())
    { super.remove(key);
    }
  }

  /**
   * Return the first element of the array mapped to the specified key
   */
  @Override
  public T getOne(K key)
  { 
    List<T> list=get(key);
    if (list==null || list.size()==0)
    { return null;
    }
    else
    { return list.get(0);
    }
  }
  
  /**
   * Add all the values to the specified list, and return the number of
   *   values added
   * 
   * @param target The list to append to
   * @return The number of values added
   */
  public int toValueList(List<T> target)
  {
    int i=0;
    for (List<T> list: values())
    { 
      i+=list.size();
      target.addAll(list);
    }
    return i;
  }

}
