package spiralcraft.lang.optics;

import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import spiralcraft.beans.MappedBeanInfo;

import spiralcraft.lang.BindException;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Optic;

/**
 * A lense associated with a single bean property. The 'get' transformation
 *   simply retrieves the value of the property from the supplied bean.
 */
class BeanPropertyLense
  implements Lense
{
  private static final Object[] EMPTY_PARAMS=new Object[0];

  private final PropertyDescriptor _property;
  private final Method _readMethod;
  private final MappedBeanInfo _beanInfo;
  private final Prism _prism;
  
  public BeanPropertyLense(PropertyDescriptor property,MappedBeanInfo beanInfo)
    throws BindException
  { 
    _property=property;
    _readMethod=property.getReadMethod();
    _beanInfo=beanInfo;
    _prism=OpticFactory.getInstance().findPrism(_property.getPropertyType());
    
  }

  public MappedBeanInfo getBeanInfo()
  { return _beanInfo;
  }
  
  public PropertyDescriptor getProperty()
  { return _property;
  }

  public Object translateForGet(Object value,Optic[] modifiers)
  { 
    try
    {
      if (_readMethod!=null)
      { 
        if (value!=null)
        { return _readMethod.invoke(value,EMPTY_PARAMS);
        }
        else
        { return null;
        }
      }
      else
      { 
        System.err.println
          ("Cannot read property '"
          +value.getClass().getName()
          +"."+_property.getName()+"'"
          );
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

  public Object translateForSet(Object val,Optic[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Prism getPrism()
  { return _prism;
  }

}

