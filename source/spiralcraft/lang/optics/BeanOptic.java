package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

import java.util.HashMap;

import java.lang.reflect.Field;

/**
 * An Optic which uses Java Beans introspection and reflection
 *   to navigate a Java object provided by a source optic.
 */
public class BeanOptic
  extends ProxyOptic
{
  private static final BeanInfoCache _BEAN_INFO_CACHE
    =new BeanInfoCache(Introspector.IGNORE_ALL_BEANINFO);

  private MappedBeanInfo _beanInfo;
  private HashMap _properties;
  private HashMap _fields;
  
  public BeanOptic(Optic source)
    throws BindException
  { 
    super(source);
    try
    {
      _beanInfo
        =_BEAN_INFO_CACHE.getBeanInfo
          (source.getTargetClass());
    }
    catch (IntrospectionException x)
    { throw new BindException("Error introspecting "+source.getTargetClass(),x);
    }
  }

  public synchronized Optic resolve(Focus focus,String name,Expression[] params)
    throws BindException
  { 
    Optic optic=null;
    if (params==null || params.length==0)
    { 
      optic=getProperty(name);
      if (optic==null)
      { optic=getField(name);
      }
    }
        
    
    return optic;
  }

  private synchronized Optic getField(String name)
  {
    Optic optic=null;
    if (_fields==null)
    { _fields=new HashMap();
    }
    else
    { optic=(Optic) _fields.get(name);
    }

    if (optic==null)
    {
      Field field
        =_beanInfo.findField(name);
      if (field!=null)
      { 
        optic
          =OpticFactory.decorate
            (new BeanFieldOptic(this,field)
            );
        _fields.put(name,optic);
      }
    }
    return optic;
  }

  private synchronized Optic getProperty(String name)
  {
    Optic optic=null;
    if (_properties==null)
    { _properties=new HashMap();
    }
    else
    { optic=(Optic) _properties.get(name);
    }

    if (optic==null)
    {
      PropertyDescriptor property
        =_beanInfo.findProperty(name);
      if (property!=null)
      { 
        optic
          =OpticFactory.decorate
            (new BeanPropertyOptic(this,property)
            );
        _properties.put(name,optic);
      }
    }
    return optic;
  }
}
