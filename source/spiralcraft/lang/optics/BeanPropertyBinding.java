package spiralcraft.lang.optics;

import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class BeanPropertyBinding
  extends LenseBinding
{
  private static final Object[] EMPTY_PARAMS=new Object[0];
  private final Object[] _params=new Object[1];
  private final PropertyDescriptor _property;
  private final Method _readMethod;
  private final boolean _static;

  public BeanPropertyBinding
    (Binding source
    ,BeanPropertyLense lense
    )
  {
    super(source,lense,null);
    _property=lense.getProperty();
    _readMethod=_property.getReadMethod();

    if (_property.getWriteMethod()==null
        && isSourceStatic()
        )
    { _static=true;
    }
    else
    { _static=false;
    }
  }

  public boolean isStatic()
  { return _static;
  }

  public synchronized boolean set(Object val)
  {
    if (_static)
    { return false;
    }

    Method method=_property.getWriteMethod();
    Object target=getSourceValue();
    try
    {
      Object oldValue=null;
      if (_readMethod!=null)
      {
        oldValue
          =_readMethod.invoke(target,EMPTY_PARAMS);
      }
      
      if (oldValue!=val || _readMethod==null)
      { 
        _params[0]=val;
        method.invoke(target,_params);
        firePropertyChange("",oldValue,val);
        return true;
      }
      else
      { return false;
      }
    }
    catch (IllegalAccessException x)
    { 
      x.printStackTrace();
      return false;
    }
    catch (InvocationTargetException x)
    { 
      x.getTargetException().printStackTrace();
      return false;
    }
  }


  public String toString()
  { 
    return super.toString()
      +":"+super.toString()
      +":[property="+_property.getName()+" ("+_property.getPropertyType()+")]";
  }
}


