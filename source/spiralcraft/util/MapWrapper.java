package spiralcraft.util;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 * A base class for map decorators
 */
public class MapWrapper
  implements Map
{
  protected final Map map;

  protected MapWrapper(Map map)
  { this.map=map;
  }

  public int size()
  { return map.size();
  } 

  public boolean isEmpty()
  { return map.isEmpty();
  }

  public boolean containsKey(Object key)
  { return map.containsKey(key);
  }

  public boolean containsValue(Object value)
  { return map.containsValue(value);
  }

  public Object get(Object key)
  { return map.get(key);
  }

  public Object put(Object key,Object value)
  { return map.put(key,value);
  }

  public Object remove(Object key)
  { return map.remove(key);
  }

  public void putAll(Map map)
  { map.putAll(map);
  }

  public void clear()
  { map.clear();
  }

  public Set keySet()
  { return map.keySet();
  }

  public Collection values()
  { return map.values();
  }

  public Set entrySet()
  { return map.entrySet();
  }
}
