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
public class KeyedList
  extends ListWrapper
{
  
  private ArrayList _keys;
  private int _numKeys;
  
  public KeyedList(List impl)
  { super(impl);
  }
  
  public Index addMap(Map implMap,KeyFunction function)
  { 
    if (_keys==null)
    { _keys=new ArrayList();
    }
    Index map=new Index(implMap,function);
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

  public boolean add(Object value)
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
    { ((Index) _keys.get(i)).getMap().clear();
    }
    super.clear();
  }
  
  public Object set(int index,Object val)
  {
    Object oldval=super.get(index);
    indexRemove(val);
    super.set(index,val);
    indexAdd(val);
    return oldval;
  }

  private void indexAdd(Object val)
  {
    for (int i=0;i<_numKeys;i++)
    { ((Index) _keys.get(i)).getMap().addValue(val);
    }
  }
  
  private void indexRemove(Object val)
  {
    for (int i=0;i<_numKeys;i++)
    { ((Index) _keys.get(i)).getMap().removeValue(val);
    }
  }
  
  /**
   * Provides access to an individual keying of a KeyedList 
   */
  public class Index
  { 
    private final AutoListMap _map;
    
    public Index(Map impl,KeyFunction function)
    { _map=new AutoListMap(impl,function);
    }
    
    AutoListMap getMap()
    { return _map;
    }
    
    public Object getOne(String key)
    { return _map.getOne(key);
    }
  }
}
