package spiralcraft.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * A data structure comprised of a List and a number of Maps. Elements
 *   inserted into this data structure are maintained in their order of
 *   insertion and are mapped according to one or more KeyFunctions.
 */
public class KeyedList<T>
  extends ListWrapper<T>
{
  
  private ArrayList<Index> _keys;
  private int _numKeys;
  
  public KeyedList(List<T> impl)
  { super(impl);
  }
  
  public Index<Object,T> addMap(Map<?,T> implMap,KeyFunction<?,T> function)
  { 
    if (_keys==null)
    { _keys=new ArrayList();
    }
    Index<Object,T> map=new Index<Object,T>(implMap,function);
    _keys.add(map);
    _numKeys++;
    return map;
  }
  
  public void removeMap(Index map)
  { 
    if (_keys!=null)
    { 
      if (_keys.remove(map))
      { _numKeys--;
      }
    }
  }

  public boolean add(T value)
  { 
    indexAdd(value);
    return super.add(value);
  }

  /**
   * Remove all entries
   */
  public void clear()
  {
    for (int i=0;i<_numKeys;i++)
    { _keys.get(i).getMap().clear();
    }
    super.clear();
  }
  
  public T set(int index,T val)
  {
    T oldval=super.get(index);
    indexRemove(val);
    super.set(index,val);
    indexAdd(val);
    return oldval;
  }

  private void indexAdd(T val)
  {
    for (int i=0;i<_numKeys;i++)
    { _keys.get(i).getMap().addValue(val);
    }
  }
  
  private void indexRemove(T val)
  {
    for (int i=0;i<_numKeys;i++)
    { _keys.get(i).getMap().removeValue(val);
    }
  }
  
  /**
   * Provides access to an individual keying of a KeyedList 
   */
  public class Index<K,T>
  { 
    private final AutoListMap<K,T> _map;
    
    public Index(Map<?,T> impl,KeyFunction<?,T> function)
    { _map=new AutoListMap<K,T>(impl,function);
    }
    
    AutoListMap<K,T> getMap()
    { return _map;
    }
    
    public T getOne(K key)
    { return _map.getOne(key);
    }
  }
}
