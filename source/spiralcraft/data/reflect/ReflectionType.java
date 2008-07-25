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

import spiralcraft.data.Type;
import spiralcraft.data.DataComposite;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.Tuple;
import spiralcraft.data.Field;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.Method;

import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.util.InstanceResolver;

import spiralcraft.data.core.TypeImpl;

import spiralcraft.util.CycleDetector;

import spiralcraft.util.lang.ClassUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.HashMap;
import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

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
      ,Date.class
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
  private Field classField;
  private boolean linked;
  
  private String[] preferredConstructorFieldNames;
  private ConstructorBinding preferredConstructorBinding;
  
  private String[] depersistMethodFieldNames;
  private String depersistMethodName;
  private MethodBinding depersistMethodBinding;

  private ThreadLocal<CycleDetector<Object>> cycleDetectorLocal
    =new ThreadLocal<CycleDetector<Object>>()
    {
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

  private static boolean checkAggregate(Class<?> clazz)
  { return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
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
        Class<?> compType=(Class<?>) iface.getTypeParameters()[0].getBounds()[0];
        arraySuffix.append(".list");
        iface=ClassUtil.boxedEquivalent(compType);
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

// This breaks things because a Tuple needs to be constructed, not an Object
// Represent this some other way than the absence of a stringConstructor
// Primary reason is to simplify syntax of string arrays where the arrays
//   can be converted to objects via a String constructor
//
//    try
//    { stringConstructor=clazz.getConstructor(String.class);
//    }
//    catch (NoSuchMethodException x)
//    { }
    
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
    throws DataException
  {
    if (linked)
    { return;
    }
    linked=true;

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
    classField=scheme.getFieldByName("class");
    
    resolvePreferredConstructor();
    resolveDepersistMethod();
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
      for (Field field: preferredConstructorBinding.getFields())
      { 
        ReflectionField rfield=(ReflectionField) field;
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
      for (Field field: depersistMethodBinding.getFields())
      { 
        ReflectionField rfield=(ReflectionField) field;
        rfield.setForcePersist(true);
        rfield.setDepersist(false);
      }
    }    
  }

  public boolean isAssignableFrom(Type<?> type)
  {
    if (!(type instanceof ReflectionType))
    { return super.isAssignableFrom(type);
    }
    else
    { return getNativeClass().isAssignableFrom(type.getNativeClass());
    }
  }
    
  public boolean isStringEncodable()
  { return stringConstructor!=null;
  }
  
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
    else
    { System.err.println("ReflectionType.fromString()  "+getURI()+": No string constructor");
    }
    return null;
    
  }
  
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
    
    if (!nativeClass.getClass().isAssignableFrom(val.getClass()))
    { throw new IllegalArgumentException("Not type compatible");
    }
    
    return val.toString();
  }

  /**
   * Obtain the instance of the bean that will have properties injected.
   *
   * Override this method in subclasses to provide alternative or context
   *   dependend constructors/resolvers
   */
  @SuppressWarnings("unchecked")
  protected T obtainInstance(Tuple tuple,InstanceResolver context)
    throws DataException
  {
    try
    {
      Class referencedClass=null;
      
      if (classField==null)
      { 
        System.err.println("No classField in "+getURI());
        referencedClass=nativeClass;
      }
      else if (classField.getValue(tuple)!=null)
      { referencedClass=(Class) classField.getValue(tuple);
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
      
      if (bean==null && preferredConstructorBinding!=null)
      { bean=(T) preferredConstructorBinding.newInstance(tuple);
      }
      
      if (bean==null)
      { 
        try
        {
          if (referencedClass.getConstructor()!=null)
          { bean=(T) referencedClass.newInstance();
          }
        }
        catch (NoSuchMethodException x)
        { 
          System.err.println
            ("ReflectionType: "+referencedClass.getName()
            +" has no default constructor"
            );
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
  public T fromData(DataComposite val,InstanceResolver context)
    throws DataException
  {
    if (val==null)
    { throw new DataException("fromData got null in "+getURI());
    }
//    System.err.println(" ReflectionType.fromData\r\nDataComposite: "+val+"\r\n");
    if (nativeClass==null)
    { 
      throw new UnsupportedOperationException
        ("Type is already represented as a Tuple");
    }

    Tuple tuple=val.asTuple();
    
    if (tupleConstructor!=null)
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
    
    if (bean!=null && depersistMethodBinding!=null)
    { depersistMethodBinding.invoke(bean,tuple);
    }
    else
    { ((ReflectionScheme) scheme).depersistBeanProperties(tuple,bean);
    }
    return bean;
  }

  
  public DataComposite toData(Object val)
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
      
      if (val.getClass()!=nativeClass)
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
  
  public String toString()
  { return super.toString()+" reflects "+this.reflectedClass.getName();
  }
  
  
}