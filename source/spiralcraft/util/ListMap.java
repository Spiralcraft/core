package spiralcraft.util;

import java.util.Map;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides an interface to a Map which maps keys to Lists for the purpose
 *   of associating multiple values with a single key
 */
public class ListMap
  extends MapWrapper
  implements MultiMap
{

  public ListMap(Map map)
  { super(map);
  }

  /**
   * Associates a key with single element array containing the
   *   specified value
   */
  public void set(Object key,Object value)
  { 
    List list=new LinkedList();
    list.add(value);
    put(key,list);
  } 

  /**
   * Append the value to the array indexed to the specified key.
   * If the array does not exist, it will be created
   */
  public void add(Object key,Object value)
  { 
    List list=(List) get(key);
    if (list==null)
    { set(key,value);
    }
    else
    { list.add(value);
    }
  }

  public void remove(Object key,Object value)
  { 
    List list=(List) get(key);
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
  public Object getOne(Object key)
  { 
    List list=(List) get(key);
    if (list==null || list.size()==0)
    { return null;
    }
    else
    { return list.get(0);
    }
  }

}
