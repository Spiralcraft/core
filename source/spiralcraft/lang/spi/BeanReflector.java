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
package spiralcraft.lang.spi;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Reflector;


import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;

import spiralcraft.util.ArrayUtil;

import spiralcraft.util.lang.MethodResolver;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Enumeration;

import java.lang.ref.WeakReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.net.URI;

import spiralcraft.log.ClassLogger;

/**
 * A Reflector which uses Java Beans introspection and reflection
 *   to navigate a Java object provided by a source optic.
 */
@SuppressWarnings("unchecked") // Various levels of heterogeneous runtime ops
public class BeanReflector<T>
  implements Reflector<T>
{
  @SuppressWarnings("unused")
  private static final ClassLogger log=new ClassLogger(BeanReflector.class);
  
  private static final boolean ENABLE_METHOD_BINDING_CACHE=true;
  
  private static final BeanInfoCache _BEAN_INFO_CACHE
    =BeanInfoCache.getInstance(Introspector.IGNORE_ALL_BEANINFO);

  
  private static final WeakHashMap<Type,WeakReference<Reflector>> reflectorMap
    =new WeakHashMap<Type,WeakReference<Reflector>>();
  

  private static final ArrayLengthTranslator arrayLengthTranslator
    =new ArrayLengthTranslator();
  

  


  
  /**
   * Find a BeanReflector which reflects the specified Java class
   */  
  // Map is heterogeneous, T is ambiguous for VoidReflector
  public static final synchronized <T> Reflector<T> 
    getInstance(Type clazz)
    throws BindException
  { 
    Reflector<T> result=null;
    WeakReference<Reflector> ref=reflectorMap.get(clazz);
    
    if (ref!=null)
    { result=ref.get();
    }
    
    if (result==null)
    {
      if (clazz==Void.class)
      { result=(Reflector<T>) new VoidReflector();
      }
      else
      { result=new BeanReflector<T>(clazz);
      }
      reflectorMap.put(clazz,new WeakReference(result));
    }
    return result;
  }
  
  
  private final MappedBeanInfo beanInfo;
  private HashMap<String,BeanPropertyTranslator<?,T>> properties;
  private HashMap<String,BeanFieldTranslator<?,T>> fields;
  private HashMap<String,MethodTranslator<?,T>> methods;
  private Class<T> targetClass;
  private Type targetType;
  private URI uri;
  private MethodResolver methodResolver;
  
  @SuppressWarnings("unchecked") // Runtime case from Type to Class<T>
  public BeanReflector(Type type)
  { 
    // log.fine(type);
    Class<T> clazz=null;
    targetType=type;
    
    if (type instanceof Class)
    { clazz=(Class<T>) type;
    }
    else if (type instanceof ParameterizedType)
    { clazz=(Class<T>) ((ParameterizedType) type).getRawType();
    }
    else
    { 
      throw new IllegalArgumentException
        ("BeanReflector: unrecognized type "+type);
    }
    
    targetClass=clazz;
    uri=BeanInfoCache.getClassURI(clazz);
    try
    {
      beanInfo
        =_BEAN_INFO_CACHE.getBeanInfo
          (targetClass);
    }
    catch (IntrospectionException x)
    { 
      x.printStackTrace();
      throw new IllegalArgumentException("Error introspecting "+clazz);
    }

  }

  public Class<T> getContentType()
  { return targetClass;
  }
  
  @Override
  public URI getTypeURI()
  { return uri;
  }

  @Override
  public boolean isAssignableTo(URI typeURI)
  { 
    Class clazz=BeanInfoCache.getClassForURI(typeURI);
    if (clazz!=null)
    { return clazz.isAssignableFrom(this.targetClass);
    }
    return false;
  }
  
  @SuppressWarnings("unchecked") // Expression array params heterogeneous
  public synchronized <X> Channel<X> 
    resolve(Channel<T> source
        ,Focus<?> focus
        ,String name
        ,Expression<?>[] params
        )
    throws BindException
  { 
    Channel<X> binding=null;
    if (params==null)
    { 
      binding=this.<X>getProperty(source,name);
      if (binding==null)
      { binding=this.<X>getField(source,name);
      }
      if (binding==null && targetClass.isArray())
      { binding=this.<X>getArrayProperty(source,name);
      }
    }
    else
    { 
      Channel[] optics=new Channel[params.length];
      for (int i=0;i<optics.length;i++)
      { optics[i]=focus.bind(params[i]);
      }
      binding=this.<X>getMethod(source,name,optics);
    }
    return binding;
  }

  @SuppressWarnings("unchecked") // Dynamic class info
  public <D extends Decorator<T>> D decorate
    (Channel<? extends T> source,Class<D> decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface==IterationDecorator.class)
    { 
      
      if (targetClass.isArray())
      { 
        Reflector reflector=BeanReflector.getInstance
          (targetClass.getComponentType());
        return (D) new ArrayIterationDecorator(source,reflector);
      }
      else if (Enumeration.class.isAssignableFrom(targetClass))
      {
        if (targetType instanceof ParameterizedType)
        {
          Type[] parameterTypes
            =((ParameterizedType) targetType).getActualTypeArguments();
          if (parameterTypes.length>0)
          {  
            Type parameterType=parameterTypes[0];
            Reflector reflector=BeanReflector.getInstance(parameterType);
            return (D) new EnumerationIterationDecorator(source,reflector);
          }
          else
          {
            // log.fine("IterationDecorator- Non-parameterized Enumeration");
            Reflector reflector=BeanReflector.getInstance(Object.class);
            return (D) new EnumerationIterationDecorator(source,reflector);
          }
        }
        else
        {
          // log.fine("IterationDecorator- Non-parameterized Enumeration");
          Reflector reflector=BeanReflector.getInstance(Object.class);
          return (D) new EnumerationIterationDecorator(source,reflector);
        }
        
      }
      else if (Iterable.class.isAssignableFrom(targetClass))
      { 
        try
        {
          // Make an effort to find a hint of a component type
          Method method=targetClass.getMethod("iterator",new Class[0]);
          Type type=method.getGenericReturnType();
          if (type instanceof ParameterizedType)
          {
            Type[] parameterTypes
              =((ParameterizedType) type).getActualTypeArguments();
            if (parameterTypes.length>0)
            { 
              Type parameterType=parameterTypes[0];
              Reflector reflector=BeanReflector.getInstance(parameterType);
              return (D) new IterableDecorator(source,reflector);
            }
            else
            {
              // log.fine("IterationDecorator- no parameters for iterator");
              Reflector reflector=BeanReflector.getInstance(Object.class);
              return (D) new IterableDecorator(source,reflector);
            }
          }
          else
          {
            // log.fine("IterationDecorator- iterator not parameterized");
            Reflector reflector=BeanReflector.getInstance(Object.class);
            return (D) new IterableDecorator(source,reflector);
          }
        
        }
        catch (NoSuchMethodException x)
        { x.printStackTrace();
        }
      }
    }
    
    // Look up the target class in the map of decorators for 
    //   the specified interface?
    return null;
  }
  
  
  @SuppressWarnings("unchecked") 
  // We are narrowing a generic type with a cast
  // When we pull the BeanFieldTranslator out of the map
  
  private synchronized <X> Channel<X> getField(Channel<T> source,String name)
    throws BindException
  {
    BeanFieldTranslator<X,T> fieldTranslator=null;
    if (fields==null)
    { fields=new HashMap<String,BeanFieldTranslator<?,T>>();
    }
    else
    { fieldTranslator=(BeanFieldTranslator<X,T>) fields.get(name);
    }

    if (fieldTranslator==null)
    {
      Field field
        =beanInfo.findField(name);
      if (field!=null)
      { 
        fieldTranslator=new BeanFieldTranslator<X,T>(field);
        fields.put(name,fieldTranslator);
      }
    }
    if (fieldTranslator!=null)
    { 
      Channel<X> binding=source.<X>getCached(fieldTranslator);
      if (binding==null)
      { 
        binding=new BeanFieldChannel<X,T>(source,fieldTranslator);
        source.cache(fieldTranslator,binding);
      }
      return binding;
    }
    return null;
  }

  @SuppressWarnings("unchecked") // Reading property from map
  private synchronized <X> Channel<X> getProperty(Channel<T> source,String name)
    throws BindException
  {
    BeanPropertyTranslator<X,T> translator=null;
    if (properties==null)
    { properties=new HashMap<String,BeanPropertyTranslator<?,T>>();
    }
    else
    { translator=(BeanPropertyTranslator<X,T>) properties.get(name);
    }

    if (translator==null)
    {
      PropertyDescriptor prop
        =beanInfo.findProperty(name);
      
      if (prop!=null)
      { 
        translator=new BeanPropertyTranslator<X,T>(prop,beanInfo);
        properties.put(name,translator);
      }
    }
    if (translator!=null)
    { 
      Channel<X> binding=source.<X>getCached(translator);
      if (binding==null)
      { 
        binding=new BeanPropertyChannel<X,T>(source,translator);
        source.cache(translator,binding);
      }
      return binding;
    }
    return null;
  }

  @SuppressWarnings("unchecked") // Reading property from map
  private synchronized <X> Channel<X> getArrayProperty(Channel<T> source,String name)
    throws BindException
  {
    Translator<X,T> translator=null;
    if (name.equals("length"))
    { translator=arrayLengthTranslator;
    }
    
    if (translator!=null)
    { 
      Channel<X> binding=source.<X>getCached(translator);
      if (binding==null)
      { 
        binding=new TranslatorChannel<X,T>(source,translator,null);
        source.cache(translator,binding);
      }
      return binding;
    }
    return null;
  }

  @SuppressWarnings("unchecked") // HashMap is heterogeneous
  private synchronized <X> Channel<X> getMethod(Channel<T> source,String name,Channel[] params)
    throws BindException
  { 
    if (methodResolver==null)
    { methodResolver=new MethodResolver(targetClass);
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

    MethodTranslator<X,T> translator=null;
    if (methods==null)
    { methods=new HashMap<String,MethodTranslator<?,T>>();
    }
    else
    { translator= (MethodTranslator<X,T>) methods.get(sig);
    }

    if (translator==null)
    {
      try
      {
        Method method
          =methodResolver.findMethod(name,classSig);
        
        if (method!=null)
        { 
          translator=new MethodTranslator<X,T>(method);
          methods.put(sig,translator);
        }
      }
      catch (NoSuchMethodException x)
      { 
        throw new BindException
          ("Method "
          +name
          +"("+ArrayUtil.format(classSig,",","")
          +") not found in "+targetClass
          ,x
          );
      }
    }
    if (translator!=null)
    { 
      if (ENABLE_METHOD_BINDING_CACHE)
      { 
        MethodKey cacheKey=new MethodKey(translator,params);
        Channel<X> binding=source.<X>getCached(cacheKey);
        if (binding==null)
        { 
          binding=new MethodChannel<X,T>(source,translator,params);
          source.cache(cacheKey,binding);
        }
        return binding;
      }
      else
      { return new MethodChannel<X,T>(source,translator,params);
      }
    }
    return null;

  }

  public String toString()
  { return super.toString()+":"+targetClass.getName();
  }



}

class MethodKey
{
  private final Object instanceSig[];

  public MethodKey(Translator<?,?> translator,Channel<?>[] params)
  {
    instanceSig=new Object[params.length+1];
    instanceSig[0]=translator;
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

class ArrayLengthTranslator<S>
  implements Translator<Integer,S>
{
  private Reflector<Integer> _reflector;
  
  public ArrayLengthTranslator()
  { 
    try
    { _reflector=BeanReflector.<Integer>getInstance(Integer.class);
    }
    catch (BindException x)
    { x.printStackTrace();
    }
  }
  
  @Override
  public Integer translateForGet(S source,Channel<?>[] params)
  { return Array.getLength(source);
  }
  
  @Override
  public S translateForSet(Integer length,Channel<?>[] params)
  { throw new UnsupportedOperationException("Cannot set array length");
  }
  
  public Reflector<Integer> getReflector()
  { return _reflector;
  }
  
}
