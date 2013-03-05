//
// Copyright (c) 1998,2007 Michael Toth
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

import spiralcraft.common.Immutable;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Type;
import spiralcraft.data.DataComposite;
import spiralcraft.data.IdentityConstructor;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.Tuple;
import spiralcraft.data.Field;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.Method;

import spiralcraft.data.lang.TupleDelegate;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.util.ConstructorInstanceResolver;
import spiralcraft.data.util.InstanceResolver;

import spiralcraft.data.core.TypeImpl;
import spiralcraft.lang.BindException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

import spiralcraft.util.CycleDetector;

import spiralcraft.util.lang.ClassUtil;
import spiralcraft.util.string.StringConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import java.util.HashMap;
import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import spiralcraft.time.Instant;

import java.net.URI;

import java.math.BigInteger;
import java.math.BigDecimal;


/**
 * A Type based on a Java class. The Scheme is defined
 *   by reflection.
 */
public class ReflectionType<T>
  extends TypeImpl<T>
{
//  private static final Level debugLevel
//    =ClassLog.getInitialDebugLevel(ReflectionType.class,null);
  
  public static final String INNER_CLASS_SEPARATOR="-";
  
  private static final HashMap<Class<?>,URI> CANONICAL_MAP
    =new HashMap<Class<?>,URI>();
  {
    
    mapStandardClass
      (Class.class
      ,Object.class 
      ,Boolean.class
      ,String.class
      ,Character.class
      ,Byte.class
      ,Short.class
      ,Integer.class
      ,Long.class
      ,Float.class
      ,Double.class
      ,BigInteger.class
      ,BigDecimal.class
      ,List.class
      ,ArrayList.class
      ,Collection.class
      ,URI.class
      ,spiralcraft.lang.Expression.class
      ,spiralcraft.lang.Binding.class
      ,Date.class
      ,Pattern.class
      ,Instant.class
      );
    
    mapSystemClass
      (java.awt.Point.class
      ,java.awt.Dimension.class
      ,java.awt.Font.class
      ,java.awt.Color.class
      );
    
  }
  
  private final Class<?> reflectedClass;
  private Constructor<T> stringConstructor;
  private Constructor<T> tupleConstructor;
  private Constructor<T> defaultConstructor;
  private Constructor<T> uriConstructor;
  
  private ReflectionField<Class<?>> classField;
  private boolean linked;
  
  private String[] preferredConstructorFieldNames;
  private ConstructorBinding preferredConstructorBinding;
  
  private String[] depersistMethodFieldNames;
  private String depersistMethodName;
  private MethodBinding depersistMethodBinding;
  
  private StringConverter<T> stringConverter;
  private final boolean immutable;

  private ThreadLocal<CycleDetector<Object>> cycleDetectorLocal
    =new ThreadLocal<CycleDetector<Object>>()
    {
      @Override
      protected synchronized CycleDetector<Object> initialValue()
      { return new CycleDetector<Object>();
      }
    };

  private static void mapStandardClass(Class<?> ... classes)
  {
    for (Class<?> clazz: classes)
    {
      CANONICAL_MAP.put
        (clazz
        ,URI.create("class:/spiralcraft/data/types/standard/"
                    .concat(clazz.getSimpleName())
                   )
        );
    }
  }
  
  private static void mapSystemClass(Class<?> ... classes)
  {
    for (Class<?> clazz: classes)
    {
      CANONICAL_MAP.put
        (clazz
        ,URI.create("class:/spiralcraft/data/types/system/"
                    .concat(clazz.getName().replace(".","/"))
                   )
        );
    }
  }
  
//  private static void mapMetaClass(Class<?> ... classes)
//  {
//    for (Class<?> clazz: classes)
//    {
//      CANONICAL_MAP.put
//        (clazz
//        ,URI.create("class:/spiralcraft/data/types/meta/"
//                    .concat(clazz.getSimpleName())
//                   )
//        );
//    }
//  }  
  
  
  public static void registerCanonicalType(Class<?> clazz,URI typeURI)
  { CANONICAL_MAP.put(clazz,typeURI);
  }

  private static boolean checkAggregate(Class<?> clazz)
  { return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
  }

  public static <X> Type<X> canonicalType(Class<X> iface)
    throws DataException
  { return Type.resolve(canonicalURI(iface));
  }
  
  public static boolean isManaged(Class<?> iface)
  { 
    return CANONICAL_MAP.get(iface)!=null
      || (iface.isArray() && isManaged(iface.getComponentType()));
  }
 
  public static URI canonicalURI(Class<?> iface)
  {
    if (iface==void.class)
    { return URI.create("class:/java/lang/Void");
    }
    
    Class<?> oiface=iface;
    iface=ClassUtil.boxedEquivalent(iface);

    if (iface==null)
    { throw new RuntimeException("Error getting boxed type for "+oiface);
    }
    
    if (Proxy.class.isAssignableFrom(iface))
    {
      iface=iface.getInterfaces()[0];
      
      ClassLog.getInstance(ReflectionType.class)
        .info("Using interface "+iface+" from proxy class ");
    }
    
    StringBuilder arraySuffix=new StringBuilder();
    while (checkAggregate(iface))
    { 
      oiface=iface;
      
      if (iface.isArray())
      {
        Class<?> compType=iface.getComponentType();
        arraySuffix.append(".array");
        iface=ClassUtil.boxedEquivalent(compType);
      }
      else
      {
        if (iface.getTypeParameters().length>0)
        {
          Class<?> compType=(Class<?>) iface.getTypeParameters()[0].getBounds()[0];
          arraySuffix.append(".list");
          iface=ClassUtil.boxedEquivalent(compType);
        }
        else
        { iface=Object.class;
        }
      }
      if (iface==null)
      { throw new RuntimeException("Error finding component type of "+oiface);
      }
    }
    
    URI uri=CANONICAL_MAP.get(iface);
    StringBuilder uriBuilder=new StringBuilder();
    if (uri!=null)
    { 
      if (arraySuffix.length()==0)
      { return uri;
      }
      else
      { uriBuilder.append(uri.toString());
      }
    }
    else
    { 
      uriBuilder
        .append("class:/")
        .append(iface.getName().replace('.','/').replace("$",INNER_CLASS_SEPARATOR));
    }

    uriBuilder.append(arraySuffix.toString());    
    
    return URI.create(uriBuilder.toString());

  }
  
  
  /** 
   * Construct a ReflectionType which reflects 'clazz' and exposes itself
   *   as Tuple data.
   */
  public ReflectionType(TypeResolver resolver,URI typeUri,Class<T> clazz)
  { 
    super(resolver,typeUri);
    reflectedClass=clazz;
    nativeClass=clazz;
    aggregate=checkAggregate(clazz);

// This used to break things because a Tuple needs to be constructed, not an Object
// But now the spiralcraft.data.sax.DataHandler respects
//   isPrimitive() 
//
// Primary reason is to simplify syntax of string arrays where the arrays
//   can be converted to objects via a String constructor
//  and otherwise simplify the creation of simple objects
    try
    { stringConstructor=clazz.getConstructor(String.class);
    }
    catch (NoSuchMethodException x)
    { }
    try
    { tupleConstructor=clazz.getConstructor(Tuple.class);
    }
    catch (NoSuchMethodException x)
    { }
    try
    { 
      defaultConstructor=clazz.getConstructor();
      if (!Modifier.isPublic(defaultConstructor.getModifiers()))
      { defaultConstructor=null;
      }
    }
    catch (NoSuchMethodException x)
    { }
    
    try
    { 
      uriConstructor=clazz.getConstructor(URI.class);
      if (!uriConstructor.isAnnotationPresent(IdentityConstructor.class))
      { uriConstructor=null;
      }
    }
    catch (NoSuchMethodException x)
    { }
    
    stringConverter=StringConverter.getInstance(clazz); 
    immutable=clazz.getAnnotation(Immutable.class)!=null;
  }
  
  
  /** 
   * Construct a ReflectionType which reflects 'clazz' and exposes itself
   *   as the specified nativeClass.
   */
  public ReflectionType
    (TypeResolver resolver
    ,URI typeUri
    ,Class<T> clazz
    ,Class<T> nativeClass
    )
  { 
    super(resolver,typeUri);
    reflectedClass=clazz;
    this.nativeClass=nativeClass;
    aggregate=checkAggregate(clazz);
    
    if (clazz.isInterface())
    { 
    }
    
    try
    { stringConstructor=clazz.getConstructor(String.class);
    }
    catch (NoSuchMethodException x)
    { }
    
    try
    { tupleConstructor=clazz.getConstructor(Tuple.class);
    }
    catch (NoSuchMethodException x)
    { }
    
    try
    { 
      defaultConstructor=clazz.getConstructor();
      if (!Modifier.isPublic(defaultConstructor.getModifiers()))
      { defaultConstructor=null;
      }
    }
    catch (NoSuchMethodException x)
    { }

    stringConverter=StringConverter.getInstance(clazz);
    immutable=clazz.getAnnotation(Immutable.class)!=null;

  }

  /**
   * 
   * @param resolver
   * @param uri
   * @return A resolver that constructs a new Type instance that uses
   *   this Type as prototype
   */
  @Override
  public InstanceResolver getExtensionResolver(TypeResolver resolver,URI uri)
  {
    return new ConstructorInstanceResolver
      (new Class<?>[] {TypeResolver.class,URI.class,Class.class}
      ,new Object[] {resolver,uri,nativeClass}
      );
  }
  
  @Override
  public boolean isPrimitive()
  { 
    if (reflectedClass.isEnum())
    { return true;
    }
    if (immutable)
    { return true;
    }
    return super.isPrimitive();
  }

  /**
   * <P>Provide the Field names that should be used for the parameters of
   *   a preferred constructor, when converting data to a native object
   *   in the fromData method.
   * 
   * <P>The constructor signature will be derived from these field names
   *   and the field values will be used as the constructor parameters.
   */
  protected void setPreferredConstructor(String ... fieldNames)
  { preferredConstructorFieldNames=fieldNames;
  }
  
  /**
   * <P>Provide a method name and a list of Field names that should be used
   *   for the parameters of a method that will be called after construction
   *   to atomically depersist a native object in the fromData method.
   * 
   * <P>The method signature will be derived from these field names
   *   and the field values will be used as the method parameters.
   */
  protected void setDepersistMethod(String methodName,String ... fieldNames)
  { 
    depersistMethodName=methodName;
    depersistMethodFieldNames=fieldNames;
  }
  
  @Override
  public void link()
  {
    if (linked)
    { return;
    }
    linked=true;
    pushLink(getURI());
    try
    {
      
      if (aggregate)
      { 
        Class<?> contentClass;
        if (reflectedClass.isArray())
        { contentClass=reflectedClass.getComponentType();
        }
        else
        { 
          contentClass=
            (Class<?>) reflectedClass.getTypeParameters()[0].getBounds()[0];
        }
        try
        { contentType=resolver.resolve(canonicalURI(contentClass));
        }
        catch (TypeNotFoundException x)
        { x.printStackTrace();
        }
        
      }

      if (reflectedClass.getSuperclass()!=null)
      { archetype=resolver.resolve(canonicalURI(reflectedClass.getSuperclass()));
      }
    
      ReflectionScheme scheme=new ReflectionScheme(resolver,this,reflectedClass);
      scheme.addFields();
      this.scheme=scheme;
    
      addMethods();
    
      super.link();
      classField
        =(ReflectionField<Class<?>>) scheme.<Class<?>>getFieldByName("class");
    
      resolvePreferredConstructor();
      resolveDepersistMethod();
    }
    catch (Exception x)
    { throw newLinkException(x);
    }
    finally
    { popLink();
    }
  }
  
  private void addMethods()
  { 
    java.lang.reflect.Method[] jmethods=reflectedClass.getDeclaredMethods();
    HashMap<String,List<Method>> map=new HashMap<String,List<Method>>();
    
    // Collate the methods by name
    for (java.lang.reflect.Method method:jmethods)
    {
      ReflectionMethod reflectMethod=new ReflectionMethod(resolver,method);
      reflectMethod.setDataType(this);
      List<Method> list=map.get(method.getName());
      if (list==null)
      { 
        list=new ArrayList<Method>();
        map.put(method.getName(),list);
      }
      list.add(reflectMethod);
      methods.add(reflectMethod);
      
    }
    for (String name: map.keySet())
    { 
      List<Method> list=map.get(name);
      Method[] methodsForName=new Method[list.size()];
      list.toArray(methodsForName);
      methodMap.put(name, methodsForName);
    }

  }
  
  private void resolvePreferredConstructor()
    throws DataException
  {
    if (preferredConstructorFieldNames!=null)
    { 
      preferredConstructorBinding
        =new ConstructorBinding
          (getScheme()
          ,getNativeClass()
          ,preferredConstructorFieldNames
          );
      for (Field<?> field: preferredConstructorBinding.getFields())
      { 
        ReflectionField<?> rfield=(ReflectionField<?>) field;
        rfield.setForcePersist(true);
        rfield.setDepersist(false);
      }
    }      
  }
  
  private void resolveDepersistMethod()
    throws DataException
  {
    if (depersistMethodFieldNames!=null)
    {
      depersistMethodBinding
        =new MethodBinding
          (getScheme()
          ,getNativeClass()
          ,depersistMethodName
          ,depersistMethodFieldNames
          );
      for (Field<?> field: depersistMethodBinding.getFields())
      { 
        ReflectionField<?> rfield=(ReflectionField<?>) field;
        rfield.setForcePersist(true);
        rfield.setDepersist(false);
      }
    }    
  }

  @Override
  public boolean isAssignableFrom(Type<?> type)
  {
    if (getNativeClass()==Aggregate.class 
        && type.isAggregate()
      )
    { 
      // Raw Aggregate accepts any pure data aggregate
      return true;
    }
    else if (!(type instanceof ReflectionType<?>)
        && !(type instanceof AssemblyType<?>)
       )
    { return super.isAssignableFrom(type);
    }
    else
    { return getNativeClass().isAssignableFrom(type.getNativeClass());
    }
  }
    
  @Override
  public boolean isStringEncodable()
  { return stringConstructor!=null || stringConverter!=null;
  }
  
  @Override
  public T fromString(String val)
    throws DataException
  {
    if (nativeClass==null)
    { 
      throw new UnsupportedOperationException
        ("Type has no String representation");
    }
    
    if (stringConstructor!=null)
    {
      try
      { return stringConstructor.newInstance(val);
      }
      catch (InstantiationException x)
      { throw new DataException("Error decoding String '"+val+"'",x);
      }
      catch (IllegalAccessException x)
      { throw new DataException("Error decoding String '"+val+"'",x);
      }
      catch (InvocationTargetException x)
      { throw new DataException("Error decoding String '"+val+"'",x);
      }
    }
    else if (stringConverter!=null)
    { return stringConverter.fromString(val);
    }
    else
    { 
      log.info
        ("ReflectionType.fromString()  "+getURI()+": No string constructor");
    }
    return null;
    
  }
  
  @Override
  public String toString(T val)
  {
    if (nativeClass==null)
    { 
      throw new UnsupportedOperationException
        ("Type has no String representation");
    }
    
    if (val==null)
    { return null;
    }
    
    if (!nativeClass.isAssignableFrom(val.getClass()))
    { 
      throw new IllegalArgumentException
        ("Not type compatible "+nativeClass+" <-- "+val.getClass());
    }
    
    if (stringConverter!=null)
    { return stringConverter.toString(val);
    }
    else
    { return val.toString();
    }
  }

  /**
   * Obtain the instance of the bean that will have properties injected.
   *
   * Override this method in subclasses to provide alternative or context
   *   dependend constructors/resolvers
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected T obtainInstance(Tuple tuple,InstanceResolver context)
    throws DataException
  {
    try
    {
      Class referencedClass=null;
      
      if (classField==null)
      { 
        if (!nativeClass.isInterface())
        { log.info("No classField in "+getURI());
        }
        referencedClass=nativeClass;
      }
      else if (classField.getValue(tuple)!=null)
      { referencedClass=classField.getValue(tuple);
      }
      else
      { referencedClass=nativeClass;
      }
     
      T bean=null;
      
      if (context!=null)
      { 
        bean=(T) context.resolve(referencedClass);
//        System.err.println("ReflectionType: resolved from context: "+bean);
      }
      
      if (!referencedClass.isInterface())
      { 
        if (bean==null && preferredConstructorBinding!=null)
        { bean=(T) preferredConstructorBinding.newInstance(tuple);
        }
      
        if (bean==null)
        { 
          if (!Modifier.isAbstract(referencedClass.getModifiers()))
          {
            

          
            if (defaultConstructor!=null)
            { bean=(T) referencedClass.newInstance();
            }
            else 
            { 
              log.log
                (Level.TRACE
                ,"ReflectionType: "+this
                +" has no default constructor"
                ,new Exception()
                );
            }

          }
          else
          { 
            throw new DataException
              ("Class not instanitable. "+referencedClass.getName()
              +" is an abstract class"
              ); 
          }
        }
      }
      return bean;
    }
    catch (InstantiationException x)
    { 
      throw new DataException
        (getURI().toString()+": Error instantiating bean from Tuple '"+tuple+"':"+x.toString(),x);
    }
    catch (IllegalAccessException x)
    { 
      throw new DataException
        (getURI().toString()+"Error instantiating bean from Tuple '"+tuple+"':"+x.toString(),x);
    }
  }
  
  /**
   * Construct an Object using reflection to inject Bean properties.
   */
  @SuppressWarnings("unchecked")
  @Override
  public T fromData(DataComposite val,InstanceResolver context)
    throws DataException
  {
    link();
    if (val==null)
    { throw new DataException("fromData got null in "+getURI());
    }
//    System.err.println(" ReflectionType.fromData\r\nDataComposite: "+val+"\r\n");
    if (nativeClass==null)
    { 
      throw new UnsupportedOperationException
        ("Type is already represented as a Tuple");
    }

    if (!val.isTuple())
    { 
      throw new DataException
        ("Expected a Tuple, not a "+val.getType()
        +" as data for type "+getURI()
        );
    }
    Tuple tuple=val.asTuple();
    
    if (tupleConstructor!=null && context==null)
    { 
      try
      { return tupleConstructor.newInstance(tuple);
      }
      catch (InstantiationException x)
      { throw new DataException("Error decoding Tuple '"+tuple+"'",x);
      }
      catch (IllegalAccessException x)
      { throw new DataException("Error decoding Tuple '"+tuple+"'",x);
      }
      catch (InvocationTargetException x)
      { throw new DataException("Error decoding Tuple '"+tuple+"'",x);
      }
    }

    T bean=obtainInstance(tuple,context);
    if (reflectedClass.isInterface())
    {
      if (bean==null)
      {
        try
        { bean=new TupleDelegate<T>((Class<T>) reflectedClass,(Tuple) val).get();
        }
        catch (BindException x)
        { 
          throw new DataException
            ("Error creating TupleDelegate for "+reflectedClass.getName()
            );
        }
      }
    }
    else
    {
    
      try
      {
        if (bean!=null)
        {
          if (depersistMethodBinding!=null)
          { depersistMethodBinding.invoke(bean,tuple);
          }
          else
          { ((ReflectionScheme) scheme).depersistBeanProperties(tuple,bean);
          }
        }
        else
        { throw new DataException("Instance of "+uri+" could not be created");
        }
      }
      catch (DataException x)
      { 
        throw new DataException
          ("Error creating instance of "
          +getNativeClass()+" from data with datatype "+val.getType().getURI()
          ,x
          );
      } 
    }
    return bean;
  }

  
  @SuppressWarnings("rawtypes")
  @Override
  public DataComposite toData(T val)
    throws DataException
  {
    if (cycleDetectorLocal.get().detectOrPush(val))
    { return null;
    }
    try
    {
      
      if (nativeClass==null)
      { 
        throw new UnsupportedOperationException
          ("Type is already represented as a Tuple");
      }
      
      if (val==null)
      { return null;
      }
  
      if (!nativeClass.isAssignableFrom(val.getClass()))
      { 
        throw new IllegalArgumentException
          (nativeClass.getName()
          +" cannot be assigned a "
          +val.getClass().getName()
          );
      }
      
      if (Proxy.class.isAssignableFrom(val.getClass()))
      {
        InvocationHandler handler=Proxy.getInvocationHandler(val);
        if (handler instanceof TupleDelegate<?>)
        { return ((TupleDelegate) handler).getTuple().snapshot();
        }
        else
        {
          // System.out.println("Not narrowing "+val.getClass()+":"+ val.toString());
          EditableTuple tuple=new EditableArrayTuple(scheme);
          ((ReflectionScheme) scheme).persistBeanProperties(val,tuple);
          return tuple;
        }
        
      }
      else if (val.getClass()!=nativeClass)          
      {
        // System.out.println("Narrowing "+val.getClass());
        Type<? super Object> type
          =resolver.resolve(canonicalURI(val.getClass()));
        return type.toData(val);
      }
      else
      {
        // System.out.println("Not narrowing "+val.getClass()+":"+ val.toString());
        EditableTuple tuple=new EditableArrayTuple(scheme);
        ((ReflectionScheme) scheme).persistBeanProperties(val,tuple);
        return tuple;
      }
    }
    finally
    { cycleDetectorLocal.get().pop();
    }
      
  }
  
  @Override
  public String toString()
  { return super.toString()+" reflects "+this.reflectedClass.getName();
  }
 
  @Override
  public boolean isDataEncodable()
  { 
    if (immutable && tupleConstructor==null)
    { return false;
    }

//    log.fine(getURI().toString()+":"+immutable+":"+tupleConstructor);
    return super.isDataEncodable();
  }  
  
}