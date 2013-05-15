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
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <p>A data structure comprised of a List and a number of Maps. Elements
 *   inserted into this data structure are maintained in their order of
 *   insertion and are mapped according to one or more KeyFunctions.
 * </p>
 */
public class KeyedList<T>
  extends ListWrapper<T>
{
  
  private ArrayList<Index<?,T>> _keys;
  private int _numKeys;
  
  public KeyedList()
  { super(new ArrayList<T>());
  }
  
  public KeyedList(List<T> impl)
  { super(impl);
  }
  
  public <K> Index<K,T> addMap(Map<K,List<T>> implMap,KeyFunction<K,T> function)
  { 
    if (_keys==null)
    { _keys=new ArrayList<Index<?,T>>();
    }
    Index<K,T> map=new Index<K,T>(implMap,function);
    _keys.add(map);
    _numKeys++;
    for (T t:this)
    { map.getMap().addValue(t);
    }
    return map;
  }
  
  public void removeMap(Index<Object,T> map)
  { 
    if (_keys!=null)
    { 
      if (_keys.remove(map))
      { _numKeys--;
      }
    }
  }

  @Override
  public boolean add(T value)
  { 
    indexAdd(value);
    return super.add(value);
  }

  /**
   * Remove all entries
   */
  @Override
  public void clear()
  {
    for (int i=0;i<_numKeys;i++)
    { _keys.get(i).getMap().clear();
    }
    super.clear();
  }
  
  @Override
  public T set(int index,T val)
  {
    T oldval=super.get(index);
    indexRemove(oldval);
    super.set(index,val);
    indexAdd(val);
    return oldval;
  }
  
  public void replace(T oldval,T newval)
  { 
    int index=super.indexOf(oldval);
    if (index<0)
    { throw new NoSuchElementException("Value not in list: "+oldval);
    }
    else
    { set(index,newval);
    } 
  }
  

  @SuppressWarnings("unchecked")
  @Override
  public boolean remove(Object val)
  {
    if (super.remove(val))
    { 
      indexRemove((T) val);
      return true;
    }
    else
    { return false;
    }
    
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
    
    public List<Tdata> get(Tkey key)
    { return _map.get(key);
    }
    
    public Tdata getFirst(Tkey key)
    { return _map.getFirst(key);
    }
    
    public Set<Tkey> keySet()
    { return _map.keySet();
    }
    
    public boolean containsKey(Tkey key)
    { 
      List<Tdata> list=get(key);
      if (list!=null && !list.isEmpty())
      { return true;
      }
      return false;
    }
  }
}
