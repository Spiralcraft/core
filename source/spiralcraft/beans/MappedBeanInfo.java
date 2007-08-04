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

import java.util.HashMap;

import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyChangeListener;

import java.lang.reflect.Field;
import java.beans.IntrospectionException;

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
 *   binding, and object data representation (ie. Builder, Lang and Tuple).
 */
public class MappedBeanInfo
  extends BeanInfoProxy
{
  private HashMap<String,PropertyDescriptor> _propertyMap;
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
    _propertyMap=new HashMap<String,PropertyDescriptor>(props.length);

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
}
