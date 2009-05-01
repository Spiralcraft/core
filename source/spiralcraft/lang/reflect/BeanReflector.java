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
package spiralcraft.lang.reflect;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.TypeModel;
import spiralcraft.lang.spi.ArrayEqualityTranslator;
import spiralcraft.lang.spi.ArrayIndexChannel;
import spiralcraft.lang.spi.ArrayIterationDecorator;
import spiralcraft.lang.spi.ArraySelectChannel;
import spiralcraft.lang.spi.CollectionSelectChannel;
import spiralcraft.lang.spi.EnumerationIterationDecorator;
import spiralcraft.lang.spi.IterableDecorator;
import spiralcraft.lang.spi.MapIndexTranslator;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;
import spiralcraft.lang.spi.VoidReflector;


import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;


import spiralcraft.util.ArrayUtil;

import spiralcraft.util.lang.MethodResolver;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Enumeration;

import java.lang.ref.WeakReference;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.net.URI;

import spiralcraft.log.ClassLog;

/**
 * A Reflector which uses Java Beans introspection and reflection
 *   to navigate a Java object provided by a source optic.
 */
@SuppressWarnings("unchecked") // Various levels of heterogeneous runtime ops
public class BeanReflector<T>
  extends Reflector<T>
{
  { 
    // Tickle the TypeModel so we can dynamically resolve these types
    BeanTypeModel.getInstance();
  }
  
  private static enum CollectionType
  {
    ARRAY
    ,MAP
    ,LIST
    ,COLLECTION
  }

  @SuppressWarnings("unused")
  private static final ClassLog log=ClassLog.getInstance(BeanReflector.class);
  
  private static final boolean ENABLE_METHOD_BINDING_CACHE=true;
  
  private static final BeanInfoCache _BEAN_INFO_CACHE
    =BeanInfoCache.getInstance(Introspector.IGNORE_ALL_BEANINFO);
  
  private static final WeakHashMap<Type,WeakReference<Reflector>> reflectorMap
    =new WeakHashMap<Type,WeakReference<Reflector>>();
  

  private static final ArrayLengthTranslator arrayLengthTranslator
    =new ArrayLengthTranslator();


  private static final Translator booleanArrayEqualityTranslator
    =new ArrayEqualityTranslator<boolean[]>()
  {
    @Override
    public boolean compare(boolean[] source,boolean[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final Translator byteArrayEqualityTranslator
    =new ArrayEqualityTranslator<byte[]>()
  {
    @Override
    public boolean compare(byte[] source,byte[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final Translator charArrayEqualityTranslator
    =new ArrayEqualityTranslator<char[]>()
  {
    @Override
    public boolean compare(char[] source,char[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final Translator shortArrayEqualityTranslator
    =new ArrayEqualityTranslator<short[]>()
  {
    @Override
    public boolean compare(short[] source,short[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final Translator intArrayEqualityTranslator
    =new ArrayEqualityTranslator<int[]>()
  {
    @Override
    public boolean compare(int[] source,int[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final Translator longArrayEqualityTranslator
    =new ArrayEqualityTranslator<long[]>()
  {
    @Override
    public boolean compare(long[] source,long[] target)
    { return Arrays.equals(source,target);
    }
  };
  
  private static final Translator floatArrayEqualityTranslator
    =new ArrayEqualityTranslator<float[]>()
  {
    @Override
    public boolean compare(float[] source,float[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final Translator doubleArrayEqualityTranslator
    =new ArrayEqualityTranslator<double[]>()
  {
    @Override
    public boolean compare(double[] source,double[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final Translator objectArrayEqualityTranslator
    =new ArrayEqualityTranslator<Object[]>()
  {
    @Override
    public boolean compare(Object[] source,Object[] target)
    { return Arrays.deepEquals(source,target);
    }
  };
  
  /**
   * Find a BeanReflector which reflects the specified Java class
   */  
  // Map is heterogeneous, T is ambiguous for VoidReflector
  public static final synchronized <T> Reflector<T> 
    getInstance(Type clazz)
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
  private volatile Channel<T> staticChannel;
  
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
    else if (type instanceof GenericArrayType)
    { 
      Type componentType=((GenericArrayType) type).getGenericComponentType();
      if (componentType instanceof Class)
      { clazz=(Class<T>) Array.newInstance((Class) componentType,0).getClass();
      }
      else if (componentType instanceof ParameterizedType)
      { 
        clazz=(Class<T>) Array.newInstance
          ((Class) ((ParameterizedType) componentType).getRawType(),0
            ).getClass();

      }
      else
      {
        throw new IllegalArgumentException
          ("BeanReflector: unrecognized type "+componentType
            +" "+componentType.getClass().getName());
        
      }
    }
    else
    { 
        throw new IllegalArgumentException
          ("BeanReflector: unrecognized type "+type+" "+type.getClass().getName());
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

  @Override
  public boolean isAssignableFrom(Reflector<?> other)
  { return getContentType().isAssignableFrom(other.getContentType());
  }
  
  @Override
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
  
  
  public synchronized <X> Channel<X> getStaticChannel()
  { 
    if (staticChannel==null)
    { staticChannel=new SimpleChannel(this,null,true);
    }
    return (Channel<X>) staticChannel;
  }
  
  @Override
  public synchronized <X> Channel<X> 
    resolveMeta(Channel<T> source
        ,Focus<?> focus
        ,String name
        ,Expression<?>[] params
        )
    throws BindException
  { 
    if (name.equals("@static"))
    { 
      if (BeanReflector.class.isAssignableFrom(source.getContentType()))
      { return ((BeanReflector<X>) source.get()).<X>getStaticChannel();
      }
      else
      { throw new BindException("@static not supported by "+source);
      }
    }
    else
    { 
      Channel<X> ret=super.resolveMeta(source, focus, name, params);
      if (ret!=null)
      { return ret;
      }
      else if (BeanReflector.class.isAssignableFrom(source.getContentType()))
      { 
        // Check static channel for fluent syntax
        return ((BeanReflector<?>) source.get()).getStaticChannel()
          .<X>resolve(focus,name.substring(1),params);
      }
      else
      { return null;
      }
    }
    

  }
  
  
  @Override
  public synchronized <X> Channel<X> 
    resolve(Channel<T> source
        ,Focus<?> focus
        ,String name
        ,Expression<?>[] params
        )
    throws BindException
  { 
    if (name.startsWith("@"))
    { return this.<X>resolveMeta(source,focus,name,params);
    }
    
    Channel<X> binding=null;
    if (name.equals("[]"))
    { binding=(Channel<X>) this.subscript(source,focus,params[0]);
    }
    else if (params==null)
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
      if (targetClass.isArray())
      { binding=this.<X>getArrayMethod(source,name,optics);
      }
      else
      { binding=this.<X>getMethod(source,name,optics);
      }
      if (binding==null && targetClass.isArray())
      { binding=this.<X>getArrayMethod(source,name);
      }
    }
    return binding;
  }

  @Override
  public <D extends Decorator<T>> D decorate
    (Channel<T> source,Class<D> decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface==(Class) IterationDecorator.class)
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
  

  @Override
  public Reflector<?> disambiguate(Reflector<?> alternate)
  {
    if (alternate==this || alternate.getTypeModel()!=getTypeModel())
    { return this;
    }
    else
    { return alternate.disambiguate(this);
    }
  }
  
  private synchronized <X> Channel<X> getField(Channel<T> source,String name)
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

  private synchronized <X> Channel<X> getProperty(Channel<T> source,String name)
  {
    
    BeanPropertyTranslator<X,T> translator=this.<X>getTranslator(name);
    
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

  /**
   * Get the Translator for the specified property
   * 
   * @param <X>
   * @param name
   * @return
   */
  public synchronized 
    <X> BeanPropertyTranslator<X,T> getTranslator(String name)
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
    return translator;
  }
  
  private synchronized <X> Channel<X> getArrayProperty(Channel<T> source,String name)
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
        binding=new TranslatorChannel<X,T>
          (source
          ,translator
          ,null
          );
        source.cache(translator,binding);
      }
      return binding;
    }
    return null;
  }

  private synchronized <X> Channel<X> getArrayMethod
    (Channel<T> source,String name,Channel ... params)
    throws BindException
  {
    Translator<X,T> translator=null;
    if (name.equals("equals"))
    { 
      Class atype=source.getContentType();

      if (atype==boolean[].class)
      { translator=booleanArrayEqualityTranslator;
      }
      else if (atype==byte[].class)
      { translator=byteArrayEqualityTranslator;
      }
      else if (atype==short[].class)
      { translator=shortArrayEqualityTranslator;
      }
      else if (atype==int[].class)
      { translator=intArrayEqualityTranslator;
      }
      else if (atype==long[].class)
      { translator=longArrayEqualityTranslator;
      }
      else if (atype==double[].class)
      { translator=doubleArrayEqualityTranslator;
      }
      else if (atype==float[].class)
      { translator=floatArrayEqualityTranslator;
      }
      else if (atype==char[].class)
      { translator=charArrayEqualityTranslator;
      }
      else if (Object[].class.isAssignableFrom(atype))
      { translator=objectArrayEqualityTranslator;
      }
      else
      { throw new BindException("Can't compare array type "+atype);
      }
    }
    
    if (translator!=null)
    { 
      Channel<X> binding=source.<X>getCached(translator);
      if (binding==null)
      { 
        binding=new TranslatorChannel<X,T>
          (source
          ,translator
          ,params
          );
        source.cache(translator,binding);
      }
      return binding;
    }
    return null;
  }
  
  private synchronized <X> Channel<X> 
    getMethod(Channel<T> source,String name,Channel ... params)
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

  
  private Channel<?> subscript
    (Channel<T> source
    ,Focus<?> focus
    ,Expression<?> subscript
    )
    throws BindException
  {
    
    CollectionType collectionType=null;
    
    Reflector<?> componentReflector=null;
    
    // Determine what kind of collection we're accessing and what its
    //   component type is
    if (targetClass.isArray())
    { 
      collectionType=CollectionType.ARRAY;
      componentReflector
        =BeanReflector.getInstance
          (targetClass.getComponentType());
    }
    else if (Map.class.isAssignableFrom(targetClass))
    {
      collectionType=CollectionType.MAP;
      if (targetType instanceof ParameterizedType)
      {
        componentReflector
          =BeanReflector.getInstance
            (
              ((ParameterizedType) targetType)
                .getActualTypeArguments()[1]
            );
      }
      else
      { throw new BindException
          ("Map of type '"+targetType+"' is not parameterized");
      }
    }
    else if (List.class.isAssignableFrom(targetClass))
    { 
      collectionType=CollectionType.LIST;
      if (targetType instanceof ParameterizedType)
      {
        componentReflector
          =BeanReflector.getInstance
            (
              ((ParameterizedType) targetType)
                .getActualTypeArguments()[0]
            );
      }
      else
      { throw new BindException
          ("Collection of type '"+targetType+"' is not parameterized");
      }
    }
    else if (Collection.class.isAssignableFrom(targetClass))
    { 
      collectionType=CollectionType.COLLECTION;
      if (targetType instanceof ParameterizedType)
      {
        componentReflector
          =BeanReflector.getInstance
            (
              ((ParameterizedType) targetType)
                .getActualTypeArguments()[0]
            );
      }
      else
      { throw new BindException
          ("Collection of type '"+targetType+"' is not parameterized");
      }
    }
    else
    {
      throw new BindException
        ("Don't know how to apply the [] operator to a '"+targetType);
    }
    
    ThreadLocalChannel<?> componentChannel
      =new ThreadLocalChannel(componentReflector);
    
    TeleFocus teleFocus=new TeleFocus(focus,componentChannel);
    
    Channel<?> subscriptChannel=teleFocus.bind(subscript);
    
    Class subscriptClass=subscriptChannel.getContentType();
    
    if (Integer.class.isAssignableFrom(subscriptClass)
        || Short.class.isAssignableFrom(subscriptClass)
        || Byte.class.isAssignableFrom(subscriptClass)
        )
    {
      switch (collectionType)
      {
        case ARRAY:
          return new ArrayIndexChannel
            (componentReflector
            ,source
            ,subscriptChannel
            );
//          return new TranslatorChannel
//            (source
//            ,new ArrayIndexTranslator(componentReflector)
//            ,new Channel[] {subscriptChannel}
//            );
        case LIST:
          return this.getMethod(source,"get",subscriptChannel);
        case MAP:
          return this.getMethod(source,"get",subscriptChannel);
        default:
          throw new BindException
            ("Don't know how to apply the [index] operator to a '"+targetType);
          
      }
    }
    else if 
      (Boolean.class.isAssignableFrom(subscriptClass)
      || boolean.class.isAssignableFrom(subscriptClass)
      )
    {
      switch (collectionType)
      {
        case ARRAY:
          return new ArraySelectChannel
            (source
            ,componentChannel
            ,subscriptChannel
            );
        case LIST:
        case COLLECTION:
          return new CollectionSelectChannel
            (source
            ,componentChannel
            ,subscriptChannel
            );
        default:
          throw new BindException
            ("Don't know how to apply the [select] operator to a '"+targetType);
      }
    }
    else
    {
      switch (collectionType)
      {
        case MAP:
          return new TranslatorChannel
            (source
            ,new MapIndexTranslator(componentReflector)
            ,new Channel[] {subscriptChannel}
            );
        default:
          throw new BindException
            ("Don't know how to apply the [lookup("
            +subscriptChannel.getContentType().getName()
            +")] operator to a '"+targetType);
      }
    }
  }
  
  @Override
  public String toString()
  { return super.toString()+":"+targetClass.getName();
  }

  @Override
  public TypeModel getTypeModel()
  { return BeanTypeModel.getInstance();
  }

  @Override
  public boolean accepts(Object val)
  { return val==null || targetClass.isAssignableFrom(val.getClass());
  }
  
  @Override
  public Reflector<T> subtype(T val)
  {
    if (val==null)
    { return null;
    }
    
    return BeanReflector.getInstance(val.getClass());
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
  
  @Override
  public boolean equals(Object o)
  {
    if (o instanceof Object[])
    { return ArrayUtil.arrayEquals(instanceSig,(Object[]) o);
    }
    else
    { return false;
    }
  }
  
  @Override
  public int hashCode()
  { return ArrayUtil.arrayHashCode(instanceSig);
  }
}



