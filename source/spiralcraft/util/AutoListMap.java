package spiralcraft.util;

import java.util.Map;

/**
 * A ListMap which uses a KeyFunction to automatically generate Keys for
 *   inserted values.
 */
public class AutoListMap
  extends ListMap
{
  private final KeyFunction _keyFunction;
  
  public AutoListMap(Map impl,KeyFunction function)
  { 
    super(impl);
    _keyFunction=function;
  }
  
  public void setValue(Object value)
  { set(_keyFunction.key(value),value);
  }
  
  public void removeValue(Object value)
  { remove(_keyFunction.key(value),value);
  }
  
  public void addValue(Object value)
  { add(_keyFunction.key(value),value);
  }
  
  public boolean containsValue(Object value)
  { return containsKey(_keyFunction.key(value));
  }
}
