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
package spiralcraft.lang.optics;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;

import spiralcraft.util.ArrayUtil;

import spiralcraft.util.lang.MethodResolver;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.util.HashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Array;

/**
 * A Prism which uses Java Beans introspection and reflection
 *   to navigate a Java object provided by a source optic.
 */
public class BeanPrism<T>
  implements Prism<T>
{
  private static final boolean ENABLE_METHOD_BINDING_CACHE=true;
  
  private static final BeanInfoCache _BEAN_INFO_CACHE
    =BeanInfoCache.getInstance(Introspector.IGNORE_ALL_BEANINFO);

  private static final ArrayLengthLense arrayLengthLense
    =new ArrayLengthLense();
  
  
  private final MappedBeanInfo _beanInfo;
  private HashMap<String,BeanPropertyLense<?,T>> _properties;
  private HashMap<String,BeanFieldLense<?,T>> _fields;
  private HashMap<String,MethodLense<?,T>> _methods;
  private Class<T> _targetClass;
  private MethodResolver _methodResolver;
  
  public BeanPrism(Class<T> clazz)
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

  public Class<T> getContentType()
  { return _targetClass;
  }
  
  
  @SuppressWarnings("unchecked") // Expression array params heterogeneous
  public synchronized <X> Binding<X> resolve(Binding<T> source,Focus<?> focus,String name,Expression[] params)
    throws BindException
  { 
    Binding<X> binding=null;
    if (params==null)
    { 
      binding=this.<X>getProperty(source,name);
      if (binding==null)
      { binding=this.<X>getField(source,name);
      }
      if (binding==null && _targetClass.isArray())
      { binding=this.<X>getArrayProperty(source,name);
      }
    }
    else
    { 
      Optic[] optics=new Optic[params.length];
      for (int i=0;i<optics.length;i++)
      { optics[i]=focus.bind(params[i]);
      }
      binding=this.<X>getMethod(source,name,optics);
    }
    return binding;
  }

  public Decorator<T> decorate(Binding<? extends T> source,Class decoratorInterface)
    throws BindException
  { 
    // Look up the target class in the map of decorators for 
    //   the specified interface. 
    return null;
  }
  
  
  @SuppressWarnings("unchecked") 
  // We are narrowing a generic type with a cast
  // When we pull the BeanFieldLense out of the map
  
  private synchronized <X> Binding<X> getField(Binding<T> source,String name)
    throws BindException
  {
    BeanFieldLense<X,T> fieldLense=null;
    if (_fields==null)
    { _fields=new HashMap<String,BeanFieldLense<?,T>>();
    }
    else
    { fieldLense=(BeanFieldLense<X,T>) _fields.get(name);
    }

    if (fieldLense==null)
    {
      Field field
        =_beanInfo.findField(name);
      if (field!=null)
      { 
        fieldLense=new BeanFieldLense<X,T>(field);
        _fields.put(name,fieldLense);
      }
    }
    if (fieldLense!=null)
    { 
      Binding<X> binding=source.getCache().<X>get(fieldLense);
      if (binding==null)
      { 
        binding=new BeanFieldBinding<X,T>(source,fieldLense);
        source.getCache().put(fieldLense,binding);
      }
      return binding;
    }
    return null;
  }

  @SuppressWarnings("unchecked") // Reading property from map
  private synchronized <X> Binding<X> getProperty(Binding<T> source,String name)
    throws BindException
  {
    BeanPropertyLense<X,T> lense=null;
    if (_properties==null)
    { _properties=new HashMap<String,BeanPropertyLense<?,T>>();
    }
    else
    { lense=(BeanPropertyLense<X,T>) _properties.get(name);
    }

    if (lense==null)
    {
      PropertyDescriptor prop
        =_beanInfo.findProperty(name);
      
      if (prop!=null)
      { 
        lense=new BeanPropertyLense<X,T>(prop,_beanInfo);
        _properties.put(name,lense);
      }
    }
    if (lense!=null)
    { 
      Binding<X> binding=source.getCache().<X>get(lense);
      if (binding==null)
      { 
        binding=new BeanPropertyBinding<X,T>(source,lense);
        source.getCache().put(lense,binding);
      }
      return binding;
    }
    return null;
  }

  @SuppressWarnings("unchecked") // Reading property from map
  private synchronized <X> Binding<X> getArrayProperty(Binding<T> source,String name)
    throws BindException
  {
    Lense<X,T> lense=null;
    if (name.equals("length"))
    { lense=arrayLengthLense;
    }
    
    if (lense!=null)
    { 
      Binding<X> binding=source.getCache().<X>get(lense);
      if (binding==null)
      { 
        binding=new LenseBinding<X,T>(source,lense,null);
        source.getCache().put(lense,binding);
      }
      return binding;
    }
    return null;
  }

  @SuppressWarnings("unchecked") // HashMap is heterogeneous
  private synchronized <X> Binding<X> getMethod(Binding<T> source,String name,Optic[] params)
    throws BindException
  { 
    if (_methodResolver==null)
    { _methodResolver=new MethodResolver(_targetClass);
    }
    
    StringBuffer sigbuf=new StringBuffer();
    Class[] classSig=new Class[params.length];
    sigbuf.append(name);
    for (int i=0;i<params.length;i++)
    {   
      sigbuf.append(":");
      classSig[i]=params[i].getContentType();
      if (classSig[i]==Void.class)
      { classSig[i]=Object.class;
      }
      sigbuf.append(classSig[i].getName());
    }
    String sig=sigbuf.toString();

    MethodLense<X,T> lense=null;
    if (_methods==null)
    { _methods=new HashMap<String,MethodLense<?,T>>();
    }
    else
    { lense= (MethodLense<X,T>) _methods.get(sig);
    }

    if (lense==null)
    {
      try
      {
        Method method
          =_methodResolver.findMethod(name,classSig);
        
        if (method!=null)
        { 
          lense=new MethodLense<X,T>(method);
          _methods.put(sig,lense);
        }
      }
      catch (NoSuchMethodException x)
      { 
        throw new BindException
          ("Method "
          +name
          +"("+ArrayUtil.format(classSig,",","")
          +") not found in "+_targetClass
          ,x
          );
      }
    }
    if (lense!=null)
    { 
      if (ENABLE_METHOD_BINDING_CACHE)
      { 
        MethodKey cacheKey=new MethodKey(lense,params);
        Binding<X> binding=source.getCache().<X>get(cacheKey);
        if (binding==null)
        { 
          binding=new MethodBinding<X,T>(source,lense,params);
          source.getCache().put(cacheKey,binding);
        }
        return binding;
      }
      else
      { return new MethodBinding<X,T>(source,lense,params);
      }
    }
    return null;

  }

  public String toString()
  { return super.toString()+":"+_targetClass.getName();
  }

}

class MethodKey
{
  private final Object instanceSig[];

  public MethodKey(Lense lense,Optic[] params)
  {
    instanceSig=new Object[params.length+1];
    instanceSig[0]=lense;
    System.arraycopy(params,0,instanceSig,1,params.length);
  }
  
  public boolean equals(Object o)
  {
    if (o instanceof Object[])
    { return ArrayUtil.arrayEquals(instanceSig,(Object[]) o);
    }
    else
    { return false;
    }
  }
  
  public int hashCode()
  { return ArrayUtil.arrayHashCode(instanceSig);
  }
}

class ArrayLengthLense<S>
  implements Lense<Integer,S>
{
  private Prism<Integer> _prism;
  
  public ArrayLengthLense()
  { 
    try
    { _prism=OpticFactory.getInstance().<Integer>findPrism(Integer.class);
    }
    catch (BindException x)
    { x.printStackTrace();
    }
  }
  
  public Integer translateForGet(S source,Optic[] params)
  { return Array.getLength(source);
  }
  
  public S translateForSet(Integer length,Optic[] params)
  { throw new UnsupportedOperationException("Cannot set array length");
  }
  
  public Prism<Integer> getPrism()
  { return _prism;
  }
  
}
