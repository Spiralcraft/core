package spiralcraft.util;

import java.util.Map;

/**
 * A Map which uses a KeyFunction to automatically generate Keys for
 *   inserted values.
 */
public class AutoMap
  extends MapWrapper
{
  private final KeyFunction _keyFunction;
  
  public AutoMap(Map impl,KeyFunction function)
  { 
    super(impl);
    _keyFunction=function;
  }
  
  public void put(Object value)
  { put(_keyFunction.key(value),value);
  }
  
  public void removeValue(Object value)
  { remove(_keyFunction.key(value));
  }
  
  public boolean containsValue(Object value)
  { return containsKey(_keyFunction.key(value));
  }
}
