package spiralcraft.util;

import java.util.Map;
import java.util.List;

/**
 * A ListMap which uses a KeyFunction to automatically generate Keys for
 *   inserted values.
 */
public class AutoListMap<K,T>
  extends ListMap<K,T>
{
  private final KeyFunction<K,T> _keyFunction;
  
  public AutoListMap(Map impl,KeyFunction function)
  { 
    super(impl);
    _keyFunction=function;
  }
  
  public void setValue(T value)
  { set(_keyFunction.key(value),value);
  }
  
  public void removeValue(T value)
  { remove(_keyFunction.key(value),value);
  }
  
  public void addValue(T value)
  { add(_keyFunction.key(value),value);
  }
  
  public boolean containsValue(Object value)
  { 
      
    List<T> list=get(_keyFunction.key((T) value));
    if (list==null)
    { return false;
    }
    return list.contains(value);
      
  }
}
