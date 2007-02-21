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
package spiralcraft.data.wrapper;

import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;


import spiralcraft.data.core.SchemeImpl;
import spiralcraft.data.core.FieldImpl;

import spiralcraft.beans.BeanInfoCache;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.net.URI;

public class ReflectionScheme
  extends SchemeImpl
{
  
  private static final BeanInfoCache BEAN_INFO_CACHE
    =BeanInfoCache.getInstance(Introspector.IGNORE_ALL_BEANINFO);

  private final Class clazz;
  private final BeanInfo beanInfo;
  private final HashMap<Method,FieldImpl> methodMap
    =new HashMap<Method,FieldImpl>();
  private final TypeResolver resolver;


  public ReflectionScheme(TypeResolver resolver,Type type,Class clazz)
  {
    this.clazz=clazz;
    this.resolver=resolver;
    this.type=type;
    
    try
    { beanInfo=BEAN_INFO_CACHE.getBeanInfo(clazz);
    }
    catch (IntrospectionException x)
    { 
      throw new IllegalArgumentException
        ("Error introspecting "+clazz.getName()+": "+x.toString());
    }
  }
  
  public void resolve()
    throws DataException
  {
    List<? extends ReflectionField> fieldList
      =generateFields(beanInfo.getPropertyDescriptors());

    for (ReflectionField field: fieldList)
    { 
      addField(field);

      Method method;

      method=field.getReadMethod();
      if (method!=null)
      { methodMap.put(method,field);
      }
      
      method=field.getWriteMethod();      
      if (method!=null)
      { methodMap.put(method,field);
      }
    }
    super.resolve();
    
  }
  
  protected ReflectionField generateField(PropertyDescriptor prop)
    throws DataException
  { 
    ReflectionField field=new ReflectionField(prop);
    
    try
    { field.setType(findType(prop.getPropertyType()));
    }
    catch (TypeNotFoundException x)
    { 
      // This should NEVER happen- there always exists a Type for
      //   every java class
      x.printStackTrace();
    }
    return field;
  }
  
  protected List<? extends ReflectionField>
    generateFields(PropertyDescriptor[] propertyDescriptors)
      throws DataException
  {
    List<ReflectionField> fieldList=new ArrayList<ReflectionField>();
    
    for (PropertyDescriptor prop : beanInfo.getPropertyDescriptors())
    { 
      if (prop.getPropertyType()!=null)
      {
        ReflectionField field=generateField(prop);
        fieldList.add(field);
        
      }
    }
    return fieldList;
  }
  
  protected Type findType(Class iface)
    throws TypeNotFoundException
  { 
    URI uri=ReflectionType.canonicalUri(iface);
    return resolver.resolve(uri);
  }
  
  public Field getField(Method method)
  { return methodMap.get(method);
  }
  
  public String toString()
  { return super.toString()+":"+clazz.toString();
  }
  
  
  /**
   * Copy bean properties of the Object into the Tuple
   */
  public void persistBeanProperties(Object bean,EditableTuple tuple)
    throws DataException
  {
    for (Field field: fields)
    { 
      if (field instanceof ReflectionField)
      { ((ReflectionField) field).persistBeanProperty(bean,tuple);
      }
    }
  }
  
  
  /**
   * Copy data values from the Tuple into bean properties of the Object
   */
  public void depersistBeanProperties(Tuple tuple,Object bean)
    throws DataException
  {
    for (Field field: fields)
    { 
      if (field instanceof ReflectionField)
      { ((ReflectionField) field).depersistBeanProperty(tuple,bean);
      }
    }
  }
  
}