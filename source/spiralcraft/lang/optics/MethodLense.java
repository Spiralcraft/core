package spiralcraft.lang.optics;

import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

class MethodLense
  implements Lense
{

  private final Method _method;
  
  public MethodLense(Method method)
  { _method=method;
  }

  public Method getMethod()
  { return _method;
  }

  public Object translateForGet(Object value,Object[] params)
  { 
    if (value==null)
    { return null;
    }
    try
    { return _method.invoke(value,params);
    }
    catch (IllegalAccessException x)
    { 
      x.printStackTrace();
      return null;
    }
    catch (InvocationTargetException x)
    { 
      x.getTargetException().printStackTrace();
      return null;
    }
  }

  public Object translateForSet(Object val,Object[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Class getTargetClass()
  { return _method.getReturnType();
  }

  public String toString()
  { return getClass().getName()+":"+_method.toString();
  }
}

