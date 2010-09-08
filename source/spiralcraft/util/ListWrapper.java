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
  
  @Override
  public Object[] toArray()
  { return _list.toArray();
  }
  
  @Override
  public <X> X[] toArray(X[] array)
  { return _list.toArray(array);
  }
  
  @Override
  public T set(int pos,T value)
  { return _list.set(pos,value);
  }
  
  @Override
  public ListIterator<T> listIterator()
  { return _list.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int start)
  { return _list.listIterator(start);
  }
  
  @Override
  public List<T> subList(int start,int end)
  { return _list.subList(start,end);
  }

  @Override
  public Iterator<T> iterator()
  { return _list.iterator();
  }
  
  @Override
  public boolean retainAll(Collection<?> c)
  { return _list.retainAll(c);
  }
  
  @Override
  public boolean contains(Object o)
  { return _list.contains(o);
  }
  
  @Override
  public boolean containsAll(Collection<?> c)
  { return _list.containsAll(c);
  }

  @Override
  public boolean remove(Object o)
  { return _list.remove(o);
  }
  
  @Override
  public boolean removeAll(Collection<?> c)
  { return _list.removeAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c)
  { return _list.addAll(c);
  }

  @Override
  public boolean addAll(int start,Collection<? extends T> c)
  { return _list.addAll(start,c);
  }
  
  @Override
  public boolean add(T o)
  { return _list.add(o);
  }
  
  @Override
  public void add(int at,T o)
  { _list.add(at,o);
  }

  @Override
  public int lastIndexOf(Object o)
  { return _list.lastIndexOf(o);
  }

  @Override
  public int indexOf(Object o)
  { return _list.indexOf(o);
  }
  
  @Override
  public T remove(int pos)
  { return _list.remove(pos);
  }
  
  @Override
  public T get(int pos)
  { return _list.get(pos);
  }
  
  @Override
  public boolean isEmpty()
  { return _list.isEmpty();
  }
  
  @Override
  public void clear()
  { _list.clear();
  }
  
  @Override
  public int size()
  { return _list.size();
  }
}
