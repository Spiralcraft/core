package spiralcraft.lang.optics;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;

import spiralcraft.util.ArrayUtil;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

import java.util.HashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * An Optic which uses Java Beans introspection and reflection
 *   to navigate a Java object provided by a source optic.
 */
public class BeanPrism
  implements Prism
{
  private static final BeanInfoCache _BEAN_INFO_CACHE
    =new BeanInfoCache(Introspector.IGNORE_ALL_BEANINFO);


  private MappedBeanInfo _beanInfo;
  private HashMap _properties;
  private HashMap _fields;
  private HashMap _methods;
  private Class _targetClass;
  
  public BeanPrism(Class clazz)
  { 
    _targetClass=clazz;
    try
    {
      _beanInfo
        =_BEAN_INFO_CACHE.getBeanInfo
          (_targetClass);
    }
    catch (IntrospectionException x)
    { 
      x.printStackTrace();
      throw new IllegalArgumentException("Error introspecting "+clazz);
    }
  }

  public synchronized Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException
  { 
    Binding binding=null;
    if (params==null)
    { 
      binding=getProperty(source,name);
      if (binding==null)
      { binding=getField(source,name);
      }
    }
    else
    { 
      Optic[] optics=new Optic[params.length];
      for (int i=0;i<optics.length;i++)
      { optics[i]=focus.bind(params[i]);
      }
      binding=getMethod(source,name,optics);
    }
    return binding;
  }

  private synchronized Binding getField(Binding source,String name)
  {
    BeanFieldLense fieldLense=null;
    if (_fields==null)
    { _fields=new HashMap();
    }
    else
    { fieldLense=(BeanFieldLense) _fields.get(name);
    }

    if (fieldLense==null)
    {
      Field field
        =_beanInfo.findField(name);
      if (field!=null)
      { 
        fieldLense=new BeanFieldLense(field);
        _fields.put(name,fieldLense);
      }
    }
    if (fieldLense!=null)
    { 
      Binding binding=source.getCache().get(fieldLense);
      if (binding==null)
      { 
        binding=new BeanFieldBinding(source,fieldLense);
        source.getCache().put(fieldLense,binding);
      }
      return binding;
    }
    return null;
  }

  private synchronized Binding getProperty(Binding source,String name)
  {
    BeanPropertyLense lense=null;
    if (_properties==null)
    { _properties=new HashMap();
    }
    else
    { lense=(BeanPropertyLense) _properties.get(name);
    }

    if (lense==null)
    {
      PropertyDescriptor prop
        =_beanInfo.findProperty(name);
      
      if (prop!=null)
      { 
        lense=new BeanPropertyLense(prop,_beanInfo);
        _properties.put(name,lense);
      }
    }
    if (lense!=null)
    { 
      Binding binding=source.getCache().get(lense);
      if (binding==null)
      { 
        binding=new BeanPropertyBinding(source,lense);
        source.getCache().put(lense,binding);
      }
      return binding;
    }
    return null;
  }

  private synchronized Binding getMethod(Binding source,String name,Optic[] params)
    throws BindException
  { 
    StringBuffer sigbuf=new StringBuffer();
    Class[] classSig=new Class[params.length];
    sigbuf.append(name);
    for (int i=0;i<params.length;i++)
    {   
      sigbuf.append(":");
      classSig[i]=params[i].getTargetClass();
      sigbuf.append(classSig[i].getName());
    }
    String sig=sigbuf.toString();

    MethodLense lense=null;
    if (_methods==null)
    { _methods=new HashMap();
    }
    else
    { lense=(MethodLense) _methods.get(sig);
    }

    if (lense==null)
    {
      try
      {
        Method method
          =_targetClass.getMethod(name,classSig);
        
        if (method!=null)
        { 
          lense=new MethodLense(method);
          _methods.put(sig,lense);
        }
      }
      catch (NoSuchMethodException x)
      { 
        throw new BindException
          ("Method "
          +name
          +"("+ArrayUtil.formatToString(classSig,",","")
          +") not found in "+_targetClass
          ,x
          );
      }
    }
    if (lense!=null)
    { 
      Binding binding=source.getCache().get(lense);
      if (binding==null)
      { 
        binding=new MethodBinding(source,lense);
        source.getCache().put(lense,binding);
      }
      return binding;
    }
    return null;

  }

  public String toString()
  { return super.toString()+":"+_targetClass.getName();
  }

}
