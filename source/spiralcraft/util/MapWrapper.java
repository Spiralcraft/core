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
import java.util.Set;
import java.util.Collection;

/**
 * A base class for map decorators
 */
public class MapWrapper<K,V>
  implements Map<K,V>
{
  protected final Map<K,V> map;

  protected MapWrapper(Map<K,V> map)
  { this.map=map;
  }

  @Override
  public int size()
  { return map.size();
  } 

  @Override
  public boolean isEmpty()
  { return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key)
  { return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value)
  { return map.containsValue(value);
  }

  @Override
  public V get(Object key)
  { return map.get(key);
  }

  @Override
  public V put(K key,V value)
  { return map.put(key,value);
  }

  @Override
  public V remove(Object key)
  { return map.remove(key);
  }

  @Override
  public void putAll(Map<? extends K,? extends V> map)
  { this.map.putAll(map);
  }

  @Override
  public void clear()
  { map.clear();
  }

  @Override
  public Set<K> keySet()
  { return map.keySet();
  }

  @Override
  public Collection<V> values()
  { return map.values();
  }

  @Override
  public Set<Map.Entry<K,V>> entrySet()
  { return map.entrySet();
  }
  
  @Override
  public String toString()
  { return super.toString()+"["+map.toString()+"]";
  }
}
