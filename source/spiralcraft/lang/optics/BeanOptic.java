package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

import java.util.HashMap;


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
  
  public BeanOptic(Optic source)
    throws IntrospectionException
  { 
    super(source);
    _beanInfo
      =_BEAN_INFO_CACHE.getBeanInfo
        (source.getTargetClass());
  }

  public synchronized Optic resolve(Focus focus,String name,Expression[] params)
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
