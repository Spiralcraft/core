package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;

import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class BeanPropertyOptic
  extends AbstractOptic
{
  private static final Object[] EMPTY_PARAMS=new Object[0];
  private final Object[] _params=new Object[1];
  private final Optic _source;
  private final PropertyDescriptor _property;

  public BeanPropertyOptic
    (Optic source
    ,PropertyDescriptor property
    )
  {
    _source=source;
    _property=property;
  }

  public Class getTargetClass()
  { return _property.getPropertyType();
  }

  public Object get()
  { 
    try
    {
      return _property.getReadMethod()
        .invoke(_source.get(),EMPTY_PARAMS);
    }
    catch (IllegalAccessException x)
    { return null;
    }
    catch (InvocationTargetException x)
    { 
      x.getTargetException().printStackTrace();
      return null;
    }
  }

  public synchronized boolean set(Object val)
  {
    Method method=_property.getWriteMethod();
    if (method==null)
    { return false;
    }
    else
    { 
      _params[0]=val;
      try
      {
        method.invoke(_source.get(),_params);
        return true;
      }
      catch (IllegalAccessException x)
      { return false;
      }
      catch (InvocationTargetException x)
      { 
        x.getTargetException().printStackTrace();
        return false;
      }
    }
  }

  public String toString()
  { 
    return super.toString()
      +":"+_source.toString()
      +":[property="+_property.getName()+" ("+_property.getPropertyType()+")]";
  }
}
