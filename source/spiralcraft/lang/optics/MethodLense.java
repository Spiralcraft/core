package spiralcraft.lang.optics;

import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;

import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

class MethodLense
  implements Lense
{

  private final Method _method;
  private final Prism _prism;
  
  public MethodLense(Method method)
    throws BindException
  { 
    _method=method;
    _prism=OpticFactory.getInstance().findPrism(method.getReturnType());
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

  public Prism getPrism()
  { return _prism;
  }

  public String toString()
  { return getClass().getName()+":"+_method.toString();
  }
}

