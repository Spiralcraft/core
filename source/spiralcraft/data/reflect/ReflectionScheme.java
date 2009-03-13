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
package spiralcraft.data.reflect;

import spiralcraft.data.TypeResolver;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;


import spiralcraft.data.core.SchemeImpl;
import spiralcraft.data.core.FieldImpl;

import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class ReflectionScheme
  extends SchemeImpl
{
  
  private static final BeanInfoCache BEAN_INFO_CACHE
    =BeanInfoCache.getInstance(Introspector.IGNORE_ALL_BEANINFO);

  private final Class<?> clazz;
  private final MappedBeanInfo beanInfo;
  private final HashMap<Method,FieldImpl<?>> methodMap
    =new HashMap<Method,FieldImpl<?>>();
  protected final TypeResolver resolver;

  public ReflectionScheme(TypeResolver resolver,Type<?> type,Class<?> clazz)
  {
    this.clazz=clazz;
    this.type=type;
    this.resolver=resolver;
    
    try
    { beanInfo=BEAN_INFO_CACHE.getBeanInfo(clazz);
    }
    catch (IntrospectionException x)
    { 
      throw new IllegalArgumentException
        ("Error introspecting "+clazz.getName()+": "+x.toString());
    }
  }
  
  /**
   * Call after creating Scheme to populate fields
   */
  public void addFields()
    throws DataException
  {
    List<? extends ReflectionField> fieldList
      =generateFields(beanInfo);

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
    
  }
  
  protected ReflectionField generateField(PropertyDescriptor prop)
    throws DataException
  { 
    ReflectionField field=new ReflectionField(resolver,prop);
    field.resolveType();
    return field;
  }
  
  protected List<? extends ReflectionField>
    generateFields(MappedBeanInfo beanInfo)
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

    // TODO: Create Field objects for public Java fields of bean.
    return fieldList;
  }
  

  
  @SuppressWarnings("unchecked") // Heterogenous map
  public <X> Field<X> getField(Method method)
  { return (Field<X>) methodMap.get(method);
  }
  
  @Override
  public String toString()
  { return super.toString()+":"+clazz.toString();
  }
  
  
  /**
   * Copy bean properties of the Object into the Tuple
   */
  public void persistBeanProperties(Object bean,EditableTuple tuple)
    throws DataException
  {
    for (Field<?> field: fields)
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
    for (Field<?> field: fields)
    { 
      if (field instanceof ReflectionField)
      { ((ReflectionField) field).depersistBeanProperty(tuple,bean);
      }
    }
  }
  
}