//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.tuple.spi;

import spiralcraft.tuple.Field;
import spiralcraft.tuple.Type;

import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;

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

  private static final HashMap<Class,ReflectionScheme> _SINGLETONS
    =new HashMap<Class,ReflectionScheme>();

  private static final HashMap<Class,Type> _TYPES
    =new HashMap<Class,Type>();

  private final Class _interface;
  private final MappedBeanInfo _beanInfo;
  private final HashMap<Method,FieldImpl> _methodMap
    =new HashMap<Method,FieldImpl>();

  
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
    
    FieldListImpl<FieldImpl> fields
      =new FieldListImpl<FieldImpl>(props.length);

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
