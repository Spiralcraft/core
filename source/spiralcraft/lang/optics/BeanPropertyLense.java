package spiralcraft.lang.optics;

import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

class BeanPropertyLense
  implements Lense
{
  private static final Object[] EMPTY_PARAMS=new Object[0];

  private final PropertyDescriptor _property;
  private final Method _readMethod;
  
  public BeanPropertyLense(PropertyDescriptor property)
  { 
    _property=property;
    _readMethod=property.getReadMethod();
  }

  public PropertyDescriptor getProperty()
  { return _property;
  }

  public Object translateForGet(Object value,Object[] modifiers)
  { 
    try
    {
      if (_readMethod!=null)
      { return _readMethod.invoke(value,EMPTY_PARAMS);
      }
      else
      { 
        System.err.println("Cannot read "+value.getClass()+"."+_property.getName());
        return null;
      }
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
  { return _property.getPropertyType();
  }

}

