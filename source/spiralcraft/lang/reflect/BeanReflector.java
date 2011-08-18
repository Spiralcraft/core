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

import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Functor;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.ListDecorator;
import spiralcraft.lang.Range;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Signature;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.TypeModel;

import spiralcraft.lang.kit.AbstractReflector;
import spiralcraft.lang.spi.ArrayEqualityTranslator;
import spiralcraft.lang.spi.ArrayIndexChannel;
import spiralcraft.lang.spi.ArrayContainsChannel;
import spiralcraft.lang.spi.ArrayListDecorator;
import spiralcraft.lang.spi.ArrayRangeChannel;
import spiralcraft.lang.spi.ArraySelectChannel;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.lang.spi.EnumerationIterationDecorator;
import spiralcraft.lang.spi.GatherChannel;
import spiralcraft.lang.spi.GenericCollectionDecorator;
import spiralcraft.lang.spi.IterableDecorator;
import spiralcraft.lang.spi.IterableIndexTranslator;
import spiralcraft.lang.spi.IterableRangeChannel;
import spiralcraft.lang.spi.IterableSelectChannel;
import spiralcraft.lang.spi.IteratorIterationDecorator;
import spiralcraft.lang.spi.ListRangeChannel;
import spiralcraft.lang.spi.MapIndexTranslator;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;
import spiralcraft.lang.spi.VoidReflector;


import spiralcraft.beans.BeanInfoCache;
import spiralcraft.beans.MappedBeanInfo;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;
import spiralcraft.builder.BuilderChannel;
import spiralcraft.common.Indexable;


import spiralcraft.util.ArrayUtil;

import spiralcraft.util.lang.ClassUtil;
import spiralcraft.util.lang.MethodResolver;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Enumeration;

import java.lang.ref.WeakReference;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.net.URI;

import spiralcraft.log.ClassLog;
// import spiralcraft.log.Level;
import spiralcraft.log.Level;

/**
 * A Reflector which uses Java Beans introspection and reflection
 *   to navigate a Java object provided by a source optic.
 */
@SuppressWarnings({"unchecked","rawtypes"}) // Various levels of heterogeneous runtime ops
public class BeanReflector<T>
  extends AbstractReflector<T>
  implements Reflector<T>,Functor<T>
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
    ,INDEXABLE
    ,ITERABLE
    ,LITERAL_ELEMENT_TYPE
  }

  private static final ClassLog log=ClassLog.getInstance(BeanReflector.class);
  
  private static final boolean ENABLE_METHOD_BINDING_CACHE=true;
  
  private static final BeanInfoCache _BEAN_INFO_CACHE
    =BeanInfoCache.getInstance(Introspector.IGNORE_ALL_BEANINFO);
  
  private static final WeakHashMap<Type,WeakReference<Reflector>> reflectorMap
    =new WeakHashMap<Type,WeakReference<Reflector>>();
  

  private static final ArrayLengthTranslator arrayLengthTranslator
    =new ArrayLengthTranslator();


  private static final ArrayEqualityTranslator booleanArrayEqualityTranslator
    =new ArrayEqualityTranslator<boolean[]>()
  {
    @Override
    public boolean compare(boolean[] source,boolean[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final ArrayEqualityTranslator byteArrayEqualityTranslator
    =new ArrayEqualityTranslator<byte[]>()
  {
    @Override
    public boolean compare(byte[] source,byte[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final ArrayEqualityTranslator charArrayEqualityTranslator
    =new ArrayEqualityTranslator<char[]>()
  {
    @Override
    public boolean compare(char[] source,char[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final ArrayEqualityTranslator shortArrayEqualityTranslator
    =new ArrayEqualityTranslator<short[]>()
  {
    @Override
    public boolean compare(short[] source,short[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final ArrayEqualityTranslator intArrayEqualityTranslator
    =new ArrayEqualityTranslator<int[]>()
  {
    @Override
    public boolean compare(int[] source,int[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final ArrayEqualityTranslator longArrayEqualityTranslator
    =new ArrayEqualityTranslator<long[]>()
  {
    @Override
    public boolean compare(long[] source,long[] target)
    { return Arrays.equals(source,target);
    }
  };
  
  private static final ArrayEqualityTranslator floatArrayEqualityTranslator
    =new ArrayEqualityTranslator<float[]>()
  {
    @Override
    public boolean compare(float[] source,float[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final ArrayEqualityTranslator doubleArrayEqualityTranslator
    =new ArrayEqualityTranslator<double[]>()
  {
    @Override
    public boolean compare(double[] source,double[] target)
    { return Arrays.equals(source,target);
    }
  };

  private static final ArrayEqualityTranslator objectArrayEqualityTranslator
    =new ArrayEqualityTranslator<Object[]>()
  {
    @Override
    public boolean compare(Object[] source,Object[] target)
    { 
//      log.fine("Comparing "+ArrayUtil.format(source,",",null)
//        +" to "+ArrayUtil.format(target,",",null)
//        +" is "+Arrays.deepEquals(source,target)
//        );      
      return Arrays.deepEquals(source,target);
    }
  };
  
  /**
   * Find a BeanReflector which reflects the specified Java class
   */  
  // Map is heterogeneous, T is ambiguous for VoidReflector
  public static final synchronized <T> Reflector<T> 
    getInstance(Type clazz)
  { 
    if (clazz==null)
    { 
      throw new IllegalArgumentException
        ("BeanReflector cannot reflect a null java.lang.reflect.Type");
    }
    
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
  
  /**
   * Return a signature for a Java method
   * 
   * @param method
   * @return
   */
  public static Signature methodSignature(Method method)
  {
    Reflector[] paramTypes
      =new Reflector[method.getParameterTypes().length];
    int i=0;
    for (Type clazz:method.getParameterTypes())
    { 
      paramTypes[i++]=BeanReflector.getInstance(clazz);
    }

    return new Signature
      (method.getName()
      ,BeanReflector.getInstance(method.getReturnType())
      ,paramTypes
      );
  }
  
  
  private final MappedBeanInfo beanInfo;
  private HashMap<String,BeanPropertyTranslator<?,T>> properties;
  private HashMap<String,BeanFieldTranslator<?,T>> fields;
  private HashMap<String,MethodTranslator<?,T>> methods;
  private HashMap<String,ConstructorTranslator<T>> constructors;
  private Class<T> targetClass;
  private Type targetType;
  private URI uri;
  private MethodResolver methodResolver;
  private final Reflector<T> boxedEquivalent;
  private boolean traceResolution=false;
  
  public BeanReflector(Type type)
  { 
    if (type==null)
    { throw new IllegalArgumentException
        ("BeanReflector cannot reflect a null java.lang.reflect.Type");
    }
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
    else if ((clazz=(Class<T>) ClassUtil.getTypeArgumentAsClass(type))!=null)
    { 
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
    
    if (Functor.class.isAssignableFrom(clazz))
    { functor=true;
    }
    
    if (targetClass.isPrimitive() && !targetClass.isArray())
    { 
      boxedEquivalent
        =BeanReflector.<T>getInstance(ClassUtil.boxedEquivalent(targetClass));
    }
    else
    { boxedEquivalent=null;
    }

  }

  @Override
  public LinkedList<Signature> getSignatures(Channel source)
    throws BindException
  { 
    
    if (boxedEquivalent!=null)
    { 
      LinkedList<Signature> sigs=boxedEquivalent.getSignatures(source);
      return sigs;
    }
    
    LinkedList signatures=super.getSignatures(source);
    signatures.addFirst(new Signature("@static",source.getReflector()));
    PropertyDescriptor[] props=beanInfo.getAllProperties();
    for (PropertyDescriptor prop:props)
    { 
      Channel out=getProperty(source,prop.getName());
      if (out==null)
      { out=getField(source,prop.getName());
      }
      if (out!=null)
      { signatures.add(new Signature(prop.getName(),out.getReflector()));
      }
    }
    
    Method[] methods=targetClass.getMethods();
    for (Method method:methods)
    { 
      if (!Modifier.isStatic(method.getModifiers()))
      { signatures.add(methodSignature(method));
      }
    }
    
    return signatures;
  }
  
  
  
  @Override
  public boolean isAssignableFrom(Reflector<?> other)
  { 
    final Class otherClass=other.getContentType();
    
    if (otherClass==Void.class || otherClass==Void.TYPE)
    { return true;
    }
    
    if (otherClass.isPrimitive())
    { 
      if (getContentType().isPrimitive())
      { return getContentType().isAssignableFrom(otherClass);
      }
      else
      {
        
        // If we can assign the boxed equivalent of a primitive, we can
        //   assign the primitive type
        return getContentType()
          .isAssignableFrom(ClassUtil.boxedEquivalent(otherClass));
      }
    }
    else if (getContentType().isPrimitive())
    { 
      Class boxedType=ClassUtil.boxedEquivalent(getContentType());
      if (boxedType!=null)
      { return boxedType.isAssignableFrom(otherClass);
      }
      else
      { 
        throw new IllegalArgumentException
          ("Primitive type "+getContentType()+" cannot be unboxed");
      }
    }
    else
    { return getContentType().isAssignableFrom(other.getContentType());
    }
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
      if (Reflector.class.isAssignableFrom(source.getContentType()))
      { return (Channel<X>) ((Reflector<?>) source.get()).getStaticChannel(focus);
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
      else if (Reflector.class.isAssignableFrom(source.getContentType()))
      { 
        // Check static channel for fluent syntax
        return ((Reflector<?>) source.get()).getStaticChannel(focus)
          .<X>resolve(focus,name.substring(1),params);
      }
      else
      { return null;
      }
    }
    

  }
  
  public void setTraceResolution(boolean traceResolution)
  { this.traceResolution=traceResolution;
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
    if (traceResolution)
    { 
      log.trace
        ("Resolving in "+toString()
        +":\r\n  "+source
        +"\r\n  ."+name
        +"\r\n  "+ArrayUtil.format(params,"\r\n","")
        );
    }
    
    if (name.startsWith("@"))
    { return this.<X>resolveMeta(source,focus,name,params);
    }
    
    Channel<X> binding=null;
    if (name.equals("[]"))
    { binding=(Channel<X>) this.subscript(source,focus,params[0]);
    }
    else if (name.equals("?="))
    { binding=(Channel<X>) this.contains(source,focus,params[0]);
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
      { 
        optics[i]=focus.bind(params[i]);
        if (traceResolution)
        { log.trace("Param #"+i+" ("+params[i]+") binds to a "+optics[i].getReflector()+" from +"+optics[i]);
        }
      }
      
      if (name=="" && functor)
      { 
        if (!source.isConstant())
        { throw new BindException("Functor source must be constant "+source);
        }
        Functor<?> functor=(Functor) source.get();
        if (functor!=null)
        { binding=(Channel<X>) functor.bindChannel(focus, optics);
        }
        else
        { throw new BindException("Could not bind functor");
        }
      }
      else if (targetClass.isArray())
      { binding=this.<X>getArrayMethod(source,name,optics);
      }
      else
      { binding=this.<X>getMethod(source,name,optics);
      }
      if (binding==null && targetClass.isArray())
      { binding=this.<X>getArrayMethod(source,name);
      }
    }
    
    if (binding==null && boxedEquivalent!=null)
    { return boxedEquivalent.resolve(source,focus,name,params);
    }
    else
    { return binding;
    }
  }

  @Override
  public <D extends Decorator<T>> D decorate
    (Channel<T> source,Class<D> decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface==(Class) IterationDecorator.class
        || decoratorInterface==(Class) CollectionDecorator.class
        || decoratorInterface==(Class) ListDecorator.class
        )
    { 
      
      if (targetClass.isArray())
      { 
        Reflector reflector=BeanReflector.getInstance
          (targetClass.getComponentType());
        return (D) new ArrayListDecorator(source,reflector);
      }
      else if (Collection.class.isAssignableFrom(targetClass))
      {

        Class type
          =ClassUtil.getTypeArgumentAsClass(targetType);
        if (type!=null)
        { 
          Reflector reflector=BeanReflector.getInstance(type);
          return (D) new GenericCollectionDecorator(source,reflector);
        }
        else
        { return (D) new GenericCollectionDecorator
            (source,BeanReflector.getInstance(Object.class));
        }

        
      }
      else if (Enumeration.class.isAssignableFrom(targetClass)
               && decoratorInterface==(Class) IterationDecorator.class
               )
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
      else if (Iterator.class.isAssignableFrom(targetClass)
          && decoratorInterface==(Class) IterationDecorator.class
          )
      {
        if (targetType instanceof ParameterizedType)
        {
          Type[] parameterTypes
            =((ParameterizedType) targetType).getActualTypeArguments();
          if (parameterTypes.length>0)
          {  
            Type parameterType=parameterTypes[0];
            Reflector reflector=BeanReflector.getInstance(parameterType);
            return (D) new IteratorIterationDecorator(source,reflector);
          }
          else
          {
            // log.fine("IterationDecorator- Non-parameterized Iterator");
            Reflector reflector=BeanReflector.getInstance(Object.class);
            return (D) new IteratorIterationDecorator(source,reflector);
          }
        }
        else
        {
          // log.fine("IterationDecorator- Non-parameterized Enumeration");
          Reflector reflector=BeanReflector.getInstance(Object.class);
          return (D) new IteratorIterationDecorator(source,reflector);
        }

      }
      else if (Iterable.class.isAssignableFrom(targetClass)
               && decoratorInterface==(Class) IterationDecorator.class
               )
      { 
        try
        {
          // Make an effort to find a hint of a component type
          Method method=targetClass.getMethod("iterator",new Class[0]);
          Class type
            =ClassUtil.getTypeArgumentAsClass(method.getGenericReturnType());
          if (type==null)
          { type=Object.class;
          }
          Reflector reflector=BeanReflector.getInstance(type);
          return (D) new IterableDecorator(source,reflector);
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
    if (alternate==this)
    { 

      // Force no override of BeanReflectors by other type models using
      //   class:/x/y/z URIs
      return this;
    }
    else if (alternate.getTypeModel()!=getTypeModel())
    {
      if (this.getContentType()!=alternate.getContentType()
          && (this.getContentType().isAssignableFrom(alternate.getContentType())
              || this.getContentType()==Object[].class
              )
          )
      { 
        // XXX: This condition will never happen, because we have a 
        //   BeanReflector for every type. What we need is an runtime annotation
        //   that specifies that a particular base class has an affinity
        //   towards a particular TypeModel.
        return alternate;
      }
      else
      { return this;
      }
    }
//    else if (alternate instanceof BeanReflector)
//    {
//      if (this.getTypeURI().equals(alternate.getTypeURI()))
//      {
//        throw new IllegalArgumentException
//        ("Cannot disambiguate "+this+" from "
//        +alternate
//        );
//        
//      }
//      else
//      {
//        throw new IllegalArgumentException
//          ("Cannot disambiguate "+this.getTypeURI()+" from "
//          +alternate.getTypeURI()
//          );
//      }
//    }
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
      
      if (prop!=null && prop.getPropertyType()!=null)
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
    if (name.equals("equals") || name.equals("==") || name.equals("!="))
    { 
      Class atype=source.getContentType();
      
      ArrayEqualityTranslator<T> atranslator=null;
      
      if (atype==boolean[].class)
      { atranslator=booleanArrayEqualityTranslator;
      }
      else if (atype==byte[].class)
      { atranslator=byteArrayEqualityTranslator;
      }
      else if (atype==short[].class)
      { atranslator=shortArrayEqualityTranslator;
      }
      else if (atype==int[].class)
      { atranslator=intArrayEqualityTranslator;
      }
      else if (atype==long[].class)
      { atranslator=longArrayEqualityTranslator;
      }
      else if (atype==double[].class)
      { atranslator=doubleArrayEqualityTranslator;
      }
      else if (atype==float[].class)
      { atranslator=floatArrayEqualityTranslator;
      }
      else if (atype==char[].class)
      { atranslator=charArrayEqualityTranslator;
      }
      else if (Object[].class.isAssignableFrom(atype))
      { atranslator=objectArrayEqualityTranslator;
      }
      else
      { throw new BindException("Can't compare array type "+atype);
      }
      translator
        =(Translator<X,T>) (name.equals("!=")?atranslator.negate:atranslator);
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

    if (!Character.isJavaIdentifierStart(name.charAt(0)))
    { return null;
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
        if (traceResolution)
        { log.log(Level.TRACE,"Error finding method "+sig,x);
        }
        return null;

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
          binding=new TranslatorChannel<X,T>(source,translator,params);
          source.cache(cacheKey,binding);
        }
        return binding;
      }
      else
      { return new TranslatorChannel<X,T>(source,translator,params);
      }
    }
    else
    { 
      if (traceResolution)
      { log.trace("No resolution for method "+sig);
      }
    }
    return null;

  }

  private Channel<Boolean> contains
    (Channel<T> source
    ,Focus<?> focus
    ,Expression<?> compareItem
    )
    throws BindException
  {
    Channel<?> compareItemChannel
      =focus.bind(compareItem);
    
    if (targetClass.isArray())
    { return new ArrayContainsChannel(source,compareItemChannel);
    }
    else if (Collection.class.isAssignableFrom(targetClass))
    { return this.getMethod(source, "contains", compareItemChannel);
    }
    else
    {
      throw new BindException
        ("Don't know how to search a "+targetClass.getName()+" (not an" 
        +" array or Collection)"
        );
    }
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
      { 
        componentReflector=BeanReflector.getInstance(Object.class);
        log.info
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
      { 
        componentReflector=BeanReflector.getInstance(Object.class);
        log.info
          ("List of type '"+targetType+"' is not parameterized");
      }
    }
    else if (Indexable.class.isAssignableFrom(targetClass))
    {
      collectionType=CollectionType.INDEXABLE;
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
      { 
        componentReflector=BeanReflector.getInstance(Object.class);
        log.info("Indexable of type '"+targetType+"' is not parameterized");
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
      { 
        componentReflector=BeanReflector.getInstance(Object.class);
        log.info
          ("Collection of type '"+targetType+"' is not parameterized");
      }
    }
    else if (Iterable.class.isAssignableFrom(targetClass))
    {
      collectionType=CollectionType.ITERABLE;
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
      { 
        componentReflector=BeanReflector.getInstance(Object.class);
        log.info
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
        || Long.class.isAssignableFrom(subscriptClass)
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
        case LIST:
        case MAP:
        case INDEXABLE:
          return this.getMethod(source,"get",subscriptChannel);
        case ITERABLE:
          return new TranslatorChannel
            (source
            ,new IterableIndexTranslator(componentChannel.getReflector())
            ,new Channel[] {subscriptChannel}
            );
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
        case ITERABLE:
          return new IterableSelectChannel
            (source
            ,componentChannel
            ,subscriptChannel
            );
        default:
          throw new BindException
            ("Don't know how to apply the [select] operator to a '"+targetType);
      }
    }
    else if (Range.class.isAssignableFrom(subscriptClass))
    { 
      switch (collectionType)
      {      
        case ARRAY:
          return new ArrayRangeChannel
            (source
            ,componentReflector
            ,subscriptChannel
            );
        case LIST:
          return new ListRangeChannel
            (source
            ,subscriptChannel
            );
        case COLLECTION:
        case ITERABLE:
          return new IterableRangeChannel
            (source
            ,subscriptChannel
            );
        default:
          throw new BindException
            ("Don't know how to apply the [range] operator to a '"+targetType);
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

  @Override
  /**
   * Create a constructor channel
   */
  public Channel<T> bindChannel(
    Focus<?> focus,
    Channel<?>[] arguments)
    throws BindException
  {
    
    if (methodResolver==null)
    { methodResolver=new MethodResolver(targetClass);
    }
    
    ArrayList<Channel<?>> indexedParamList=new ArrayList<Channel<?>>();
    ArrayList<Channel<?>> namedParamList=new ArrayList<Channel<?>>();
    
    boolean endOfParams=false;
    for (Channel<?> chan : arguments)
    { 
      if (chan instanceof BindingChannel)
      { 
        endOfParams=true;
        namedParamList.add(chan);
      }
      else
      {
        if (endOfParams)
        { 
          throw new BindException
            ("Positional parameters must preceed named parameters");
        }
        indexedParamList.add(chan);
        
      }
      
    }
    
    StringBuffer sigbuf=new StringBuffer();
    Class[] classSig=new Class[indexedParamList.size()];
    sigbuf.append("");
    for (int i=0;i<indexedParamList.size();i++)
    {   
      sigbuf.append(":");
      classSig[i]=indexedParamList.get(i).getContentType();
      sigbuf.append(classSig[i].getName());
    }
    String sig=sigbuf.toString();



    Channel<T> constructorChannel=null;

    if (targetClass.isInterface())
    {
      if (indexedParamList.size()==0)
      {
        try
        {
          constructorChannel
            =new BuilderChannel
              (focus
              ,null
              ,AssemblyLoader.getInstance().findAssemblyClass(targetClass)
              );
        }
        catch (BuildException x)
        { 
          throw new BindException
            ("Error creating Assembly constructor for proxied interface "
            +targetClass
            );
        }
      }
      else
      {
        throw new BindException
          ("Proxied interface cannot have indexed constructor parameters for "
          +targetClass
          );
      }
    }
    else 
    {
      ConstructorTranslator<T> translator=null;
      if (constructors==null)
      { constructors=new HashMap<String,ConstructorTranslator<T>>();
      }
      else
      { translator= constructors.get(sig);
      }
      
      if (translator==null)
      {
        try
        {
          Constructor<T> method
            =(Constructor<T>) methodResolver.findConstructor(classSig);
        
          if (method!=null)
          { 
            translator=new ConstructorTranslator<T>(this,method);
            constructors.put(sig,translator);
          }
          else
          {
            log.log
              (Level.DEBUG
              ,"Constructor "
              +"("+ArrayUtil.format(classSig,",","")
              +") not found in "+targetClass
              );
            
          }
        }
        catch (NoSuchMethodException x)
        { 
          log.log
            (Level.DEBUG
            ,"Constructor "
            +"("+ArrayUtil.format(classSig,",","")
            +") not found in "+targetClass
            ,x
            );
          
        
        
          return null;
//          throw new BindException
//            ("Method "
//            +name
//            +"("+ArrayUtil.format(classSig,",","")
//            +") not found in "+targetClass
//            ,x
//            );
        }
      }

      // Create the channel to call the constructor
      if (translator!=null)
      { 
      
        Channel<Void> nullSource
         =new SimpleChannel<Void>
             (BeanReflector.<Void>getInstance(Void.class),null,true);
        
        Channel[] indexedParams
          =indexedParamList.toArray(new Channel[indexedParamList.size()]);      
        if (ENABLE_METHOD_BINDING_CACHE)
        { 
          MethodKey cacheKey=new MethodKey(translator,indexedParams);
          constructorChannel=getSelfChannel().<T>getCached(cacheKey);
          if (constructorChannel==null)
          { 
            constructorChannel
              =new TranslatorChannel<T,Void>(nullSource,translator,indexedParams);
            getSelfChannel().cache(cacheKey,constructorChannel);
          }
        }
        else
        { 
          constructorChannel
            =new TranslatorChannel<T,Void>(nullSource,translator,indexedParams);
        }
      
      }
    }
    
    
   
    if (constructorChannel!=null)
    { 
      if (namedParamList.size()>0)
      { 
        constructorChannel
          =new GatherChannel<T>
            (constructorChannel
            ,namedParamList.toArray
              (new BindingChannel[namedParamList.size()])
            );
      }
      
      return constructorChannel;
    }
    
    
    
    return null;


  }

}

class MethodKey
{
  private final Object instanceSig[];

  public MethodKey(Translator<?,?> translator,Channel<?>[] params)
  {
    instanceSig=new Object[params.length+1];
    instanceSig[0]=translator;
    for (int i=1;i<instanceSig.length;i++)
    { instanceSig[i]=params[i-1].getReflector();
    }
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



