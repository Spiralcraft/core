package spiralcraft.util;

import java.util.Map;


/**
 * Provides an interface to a Map which maps keys to arrays.
 */
public class ArrayMap
  extends MapWrapper
{

  private final Class _arrayComponentClass;

  public ArrayMap(Map map,Class arrayComponentClass)
  { 
    super(map);
    _arrayComponentClass=arrayComponentClass;
  }

  /**
   * Associates a key with single element array containing the
   *   specified value
   */
  public void set(Object key,Object value)
  { map.put(key,ArrayUtil.newInstance(_arrayComponentClass,value));
  } 

  /**
   * Append the value to the array indexed to the specified key.
   * If the array does not exist, it will be created
   */
  public void add(Object key,Object value)
  { 
    Object array=map.get(key);
    if (array==null)
    { set(key,value);
    }
    else
    { map.put(key,ArrayUtil.append(array,value));
    }
  }

  /**
   * Return the first element of the array mapped to the specified key
   */
  public Object getValue(Object key)
  { return ArrayUtil.getFirstElement(map.get(key));
  }

}
