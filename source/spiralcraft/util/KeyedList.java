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
  
  private ArrayList<Index<Object,T>> _keys;
  private int _numKeys;
  
  public KeyedList(List<T> impl)
  { super(impl);
  }
  
  public Index<Object,T> addMap(Map<Object,List<T>> implMap,KeyFunction<Object,T> function)
  { 
    if (_keys==null)
    { _keys=new ArrayList<Index<Object,T>>();
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
  public class Index<Tkey,Tdata>
  { 
    private final AutoListMap<Tkey,Tdata> _map;
    
    public Index(Map<Tkey,List<Tdata>> impl,KeyFunction<Tkey,Tdata> function)
    { _map=new AutoListMap<Tkey,Tdata>(impl,function);
    }
    
    AutoListMap<Tkey,Tdata> getMap()
    { return _map;
    }
    
    public Tdata getOne(Tkey key)
    { return _map.getOne(key);
    }
  }
}
