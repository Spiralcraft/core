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
package spiralcraft.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyChangeListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.beans.IntrospectionException;

import spiralcraft.log.ClassLog;
import spiralcraft.util.lang.ClassUtil;

/**
 * Extends the BeanInfo interface and implementation to enhance the performance
 *   of property, field and event lookup.
 *
 * One MappedBeanInfo is usually maintained in memory for each class referenced
 *   by clients- MappedBeanInfo objects are held by the BeanInfoCache.
 *
 * Works by caching BeanInfo descriptors in HashMaps for fast repeated 
 *   retrieval. 
 *
 * Initially geared towards supporting bean property driven IoC, language
 *   binding, and object data representation (ie. Builder, Lang and Data).
 */
public class MappedBeanInfo
  extends BeanInfoProxy
{
  private static final ClassLog log
    =ClassLog.getInstance(MappedBeanInfo.class);
  private static final boolean debug=false;
  
  private LinkedHashMap<String,PropertyDescriptor> _propertyMap;
  private HashMap<String,Field> _fieldMap;
  private Field[] _fields;
  private EventSetDescriptor _propertyChangeEventSetDescriptor=null;
  private final MappedBeanInfo superBeanInfo;
  private final MappedBeanInfo[] interfacesBeanInfo;
  
  public MappedBeanInfo(BeanInfo binf,BeanInfoCache cache)
    throws IntrospectionException
  { 
    super(binf);
    mapProperties();
    mapFields();

    EventSetDescriptor[] events
      =beanInfo.getEventSetDescriptors();
    
    for (int i=0;i<events.length;i++)
    { 
      if (events[i].getListenerType()==PropertyChangeListener.class)
      { 
        _propertyChangeEventSetDescriptor=events[i];
        break;
      }
    }
    
    Class<?> superClass=binf.getBeanDescriptor().getBeanClass().getSuperclass();
    if (superClass!=null)
    { superBeanInfo=cache.getBeanInfo(superClass);
    }
    else
    { superBeanInfo=null;
    }
    
    Class<?>[] interfaces
      =binf.getBeanDescriptor().getBeanClass().getInterfaces();
    if (interfaces!=null)
    {
      interfacesBeanInfo=new MappedBeanInfo[interfaces.length];
      int i=0;
      for (Class<?> clazz: interfaces)
      { interfacesBeanInfo[i++]=cache.getBeanInfo(clazz);
      }
      
    }
    else
    { interfacesBeanInfo=null;
    }
  }
  
  public EventSetDescriptor getPropertyChangeEventSetDescriptor()
  { return _propertyChangeEventSetDescriptor;
  }

  private void mapProperties()
  {
    PropertyDescriptor[] props
      =beanInfo.getPropertyDescriptors();
    _propertyMap=new LinkedHashMap<String,PropertyDescriptor>(props.length);

    for (int i=0;i<props.length;i++)
    { _propertyMap.put(props[i].getName(),props[i]);
    }
  }

  private void mapFields()
  {
    _fields=getBeanDescriptor().getBeanClass().getFields();
    _fieldMap=new HashMap<String,Field>(_fields.length);

    for (int i=0;i<_fields.length;i++)
    { _fieldMap.put(_fields[i].getName(),_fields[i]);
    }
  }
  
  public Field[] getFields()
  { return _fields;
  }

  /**
   * Find a property declared in this Bean or any of its 
   *   superclasses or interfaces
   * 
   * @param name The property name
   * @return The PropertyDescriptor
   */
  public PropertyDescriptor findProperty(String name)
  { 
    PropertyDescriptor ret=_propertyMap.get(name);
    if (ret==null && superBeanInfo!=null)
    { ret=superBeanInfo.findProperty(name);
    }
    if (ret==null && interfacesBeanInfo!=null)
    { 
      for (MappedBeanInfo beanInfo: interfacesBeanInfo)
      {
        ret=beanInfo.findProperty(name);
        if (ret!=null)
        { break;
        }
      }
    }
    return ret;
  }

  public Field findField(String name)
  { return _fieldMap.get(name);
  }
  
  public String recapitalize(String propertyName)
  {
    if (Character.isLowerCase(propertyName.charAt(0)))
    { 
      return Character.toUpperCase(propertyName.charAt(0))
        +(propertyName.length()>1?propertyName.substring(1):"")
        ;
    }
    else
    { return propertyName;
    }
  }
  
  /**
   * Get the actual setter Method from the actual class we're introspecting,
   *  for an extended property type,
   *  because the PropertyDescriptor might not pick up a co-variant return
   *  type
   * 
   * @param property
   * @return The read method
   */
  public Method getSpecificWriteMethod
    (PropertyDescriptor property,Class<?> type)
  {
    Method writeMethod=property.getWriteMethod();

    String searchMethod
      =writeMethod!=null
        ?writeMethod.getName()
        :"set"+recapitalize(property.getName());

    Method altWriteMethod
      =ClassUtil.getMethod
        (getBeanDescriptor().getBeanClass()
        ,searchMethod
        ,new Class[] {type}
        );
      
    if (altWriteMethod!=null)
    { writeMethod=altWriteMethod;
    }
    else
    { 
      if (debug)
      {
        log.debug
          ("No alternate write method "+searchMethod+"("+type.getName()+") in " 
          +getBeanDescriptor().getBeanClass()
          );
      }
    }

    return writeMethod;
    
  }
  
  /**
   * Returns the most specific type of either the PropertyDescriptor, the
   *   read method, or a compatible public field
   * 
   * @param property
   * @return
   */
  public Class<?> getCovariantPropertyType(PropertyDescriptor property)
  {
    Class<?> clazz=property.getPropertyType();
    
    Method readMethod=getCovariantReadMethod(property);
    if (readMethod!=null)
    { 
      Class<?> genericType
        =ClassUtil.getClass(readMethod.getGenericReturnType());
      if (genericType!=null
        && clazz.isAssignableFrom(genericType))
      {
        // Generic type is more specific
        clazz=genericType;
      }
      else
      {
        if (clazz.isAssignableFrom(readMethod.getReturnType()))
        { clazz=readMethod.getReturnType();
        }
      }
    }
  
    Field field=null;
    try
    {
      field
        =getBeanDescriptor().getBeanClass()
          .getField(property.getName());
      if (!Modifier.isPublic(field.getModifiers()))
      { field=null;
      }
      else if 
        (!clazz.isAssignableFrom(field.getType())
         && !field.getType().isAssignableFrom(clazz)
        )
      { field=null;
      }
    }
    catch (NoSuchFieldException x)
    {  
    }
  
  
   
    if (property.getWriteMethod()==null 
       && field!=null
       && field.getType()!=clazz
       )
    {
      if (readMethod==null 
         || readMethod.getReturnType().isAssignableFrom(field.getType())
       )
      { 
        // Property is writable through a public field that is typed 
        //   more specifically than the read method. We need to
        //   make the property type more specific.
        if (clazz.isAssignableFrom(field.getType()))
        { clazz=field.getType();
        }
      }
    }
    return clazz;
    
  }
  
  public String[] getAllPropertyNames()
  {
    PropertyDescriptor[] properties=getAllProperties();
    
    String[] names=new String[properties.length];
    
    int i=0;
    for (PropertyDescriptor prop:properties)
    { names[i++]=prop.getName();
    }
    return names;
  }
  
  public PropertyDescriptor[] getAllProperties()
  { 
    return getPropertyDescriptors();
//    List<PropertyDescriptor> descriptors
//      =new ArrayList<PropertyDescriptor>();
//    
//    MappedBeanInfo info=this;
//    while (info!=null)
//    { 
//      PropertyDescriptor[] localProps=info.getPropertyDescriptors();
//      for (PropertyDescriptor prop: localProps)
//      { descriptors.add(prop);
//      }
//      info=info.superBeanInfo;
//    }
//    return descriptors.toArray(new PropertyDescriptor[descriptors.size()]);
  }
  
  /**
   * Get the actual getter Method from the actual class we're introspecting,
   *  because the PropertyDescriptor might not pick up a co-variant return
   *  type
   * 
   * @param property
   * @return The read method
   */
  public Method getCovariantReadMethod
    (PropertyDescriptor property)
  {
    
    Method readMethod=property.getReadMethod();
    
    if (readMethod!=null)
    {
      try
      {
        Method altReadMethod
          =getBeanDescriptor().getBeanClass()
            .getMethod(readMethod.getName(), new Class[0]);
        if (altReadMethod!=null)
        { readMethod=altReadMethod;
        }
      }    
      catch (NoSuchMethodException x)
      { 
        log.fine
          ("NoSuchMethodException getting alt read method "
            +beanInfo.getBeanDescriptor().getBeanClass()+"."
            +readMethod.getName()+"()"
          );        
      }    
    }
    
    return readMethod;
    
  }
}
