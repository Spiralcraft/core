package spiralcraft.tuple.spi;

import spiralcraft.tuple.Field;
import spiralcraft.tuple.Type;

import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Scheme derived from reflecting the bean properties defined in a
 *   Java class or interface.
 *
 * Provides a means to define Tuples which implement
 *   Java interfaces via proxy, or which manage property sets 
 *   for Java objects.
 */
public class ReflectionScheme
  extends SchemeImpl
{
  private static final BeanInfoCache _BEAN_INFO_CACHE
    =BeanInfoCache.getInstance(Introspector.IGNORE_ALL_BEANINFO);

  private static final HashMap _SINGLETONS
    =new HashMap();

  private static final HashMap _TYPES
    =new HashMap();

  private final Class _interface;
  private final MappedBeanInfo _beanInfo;
  private final HashMap _methodMap=new HashMap();

  
  /**
   * Return the Scheme which corresponds to this Java interface
   */
  public static synchronized ReflectionScheme getInstance(Class iface)
  { 
    ReflectionScheme instance=(ReflectionScheme) _SINGLETONS.get(iface);
    if (instance==null)
    { 
      instance=new ReflectionScheme(iface);
      _SINGLETONS.put(iface,instance);
    }
    return instance;
    
  }
  
  public static synchronized Type findType(Class iface)
  { 
    TypeImpl type=(TypeImpl) _TYPES.get(iface);
    if (type==null)
    { 
      type=new TypeImpl();
      type.setJavaClass(iface);
      _TYPES.put(iface,type);
    }
    return type;
  }

  ReflectionScheme(Class iface)
  {
    _interface=iface;
    try
    { _beanInfo=_BEAN_INFO_CACHE.getBeanInfo(iface);
    }
    catch (IntrospectionException x)
    { 
      throw new IllegalArgumentException
        ("Error introspecting "+iface.getName()+": "+x.toString());
    }
    
    PropertyDescriptor[] props=
      _beanInfo.getPropertyDescriptors();
    
    FieldListImpl fields=new FieldListImpl(props.length);

    for (int i=0;i<props.length;i++)
    { 
      FieldImpl field=new FieldImpl();
      field.setName(props[i].getName());
      field.setType(findType(props[i].getPropertyType()));
      fields.add(field);

      Method method;
      
      method=props[i].getReadMethod();
      if (method!=null)
      { _methodMap.put(method,field);
      }
      
      method=props[i].getWriteMethod();
      if (method!=null)
      { _methodMap.put(method,field);
      }
      
      
    }
    setFields(fields);
  }
  
  public Field getField(Method method)
  { return (Field) _methodMap.get(method);
  }
  
  public String toString()
  { return super.toString()+":"+_interface.toString();
  }
}
