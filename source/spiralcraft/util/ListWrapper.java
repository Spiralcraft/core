package spiralcraft.util;

import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Collection;

/**
 * A base class for List decorators
 */
public abstract class ListWrapper
  implements List
{
  private final List _list;
  
  public ListWrapper(List impl)
  { _list=impl; 
  }
  
  public Object[] toArray()
  { return _list.toArray();
  }
  
  public Object[] toArray(Object[] array)
  { return _list.toArray(array);
  }
  
  public Object set(int pos,Object value)
  { return _list.set(pos,value);
  }
  
  public ListIterator listIterator()
  { return _list.listIterator();
  }

  public ListIterator listIterator(int start)
  { return _list.listIterator(start);
  }
  
  public List subList(int start,int end)
  { return _list.subList(start,end);
  }

  public Iterator iterator()
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

  public boolean addAll(Collection c)
  { return _list.addAll(c);
  }

  public boolean addAll(int start,Collection c)
  { return _list.addAll(start,c);
  }
  
  public boolean add(Object o)
  { return _list.add(o);
  }
  
  public void add(int at,Object o)
  { _list.add(at,o);
  }

  public int lastIndexOf(Object o)
  { return _list.lastIndexOf(o);
  }

  public int indexOf(Object o)
  { return _list.indexOf(o);
  }
  
  public Object remove(int pos)
  { return _list.remove(pos);
  }
  
  public Object get(int pos)
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
