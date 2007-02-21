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
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Collection;

/**
 * A base class for List decorators
 */
public  class ListWrapper<T>
  implements List<T>
{
  private final List<T> _list;
  
  public ListWrapper(List<T> impl)
  { _list=impl; 
  }
  
  public Object[] toArray()
  { return _list.toArray();
  }
  
  public <X> X[] toArray(X[] array)
  { return _list.toArray(array);
  }
  
  public T set(int pos,T value)
  { return _list.set(pos,value);
  }
  
  public ListIterator<T> listIterator()
  { return _list.listIterator();
  }

  public ListIterator<T> listIterator(int start)
  { return _list.listIterator(start);
  }
  
  public List<T> subList(int start,int end)
  { return _list.subList(start,end);
  }

  public Iterator<T> iterator()
  { return _list.iterator();
  }
  
  public boolean retainAll(Collection c)
  { return _list.retainAll(c);
  }
  
  public boolean contains(Object o)
  { return _list.contains(o);
  }
  
  public boolean containsAll(Collection c)
  { return _list.containsAll(c);
  }

  public boolean remove(Object o)
  { return _list.remove(o);
  }
  
  public boolean removeAll(Collection c)
  { return _list.removeAll(c);
  }

  public boolean addAll(Collection<? extends T> c)
  { return _list.addAll(c);
  }

  public boolean addAll(int start,Collection<? extends T> c)
  { return _list.addAll(start,c);
  }
  
  public boolean add(T o)
  { return _list.add(o);
  }
  
  public void add(int at,T o)
  { _list.add(at,o);
  }

  public int lastIndexOf(Object o)
  { return _list.lastIndexOf(o);
  }

  public int indexOf(Object o)
  { return _list.indexOf(o);
  }
  
  public T remove(int pos)
  { return _list.remove(pos);
  }
  
  public T get(int pos)
  { return _list.get(pos);
  }
  
  public boolean isEmpty()
  { return _list.isEmpty();
  }
  
  public void clear()
  { _list.clear();
  }
  
  public int size()
  { return _list.size();
  }
}
