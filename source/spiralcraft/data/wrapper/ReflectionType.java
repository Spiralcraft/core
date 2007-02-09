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
package spiralcraft.data.wrapper;

import spiralcraft.data.Type;
import spiralcraft.data.DataComposite;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.Scheme;
import spiralcraft.data.Tuple;
import spiralcraft.data.Field;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.ValidationResult;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.spi.EditableArrayTuple;

import spiralcraft.util.ClassUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.HashMap;

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
public class ReflectionType
  implements Type
{
  private static final HashMap<Class,URI> CANONICAL_MAP
    =new HashMap<Class,URI>();
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
      );
  }
  
  
  private final TypeResolver resolver;
  private final URI uri;
  private final Class reflectedClass;
  private final Class nativeType;
  private final boolean aggregate;
  private Type contentType;
  private ReflectionScheme scheme;
  private boolean linked=false;
  private Constructor stringConstructor;
  private Constructor tupleConstructor;
  
  private Field classField;
  private Type archetype;

  private static void mapStandardClass(Class ... classes)
  {
    for (Class clazz: classes)
    {
      CANONICAL_MAP.put
        (clazz
        ,URI.create("java:/spiralcraft/data/types/standard/"
                    .concat(clazz.getSimpleName())
                   )
        );
    }
  }
  
  private static boolean checkAggregate(Class clazz)
  { return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
  }

 
  public static URI canonicalUri(Class iface)
  {
    iface=ClassUtil.boxedEquivalent(iface);

    StringBuilder arraySuffix=new StringBuilder();
    while (checkAggregate(iface))
    { 
      if (iface.isArray())
      {
        Class compType=iface.getComponentType();
        arraySuffix.append(".array");
        iface=ClassUtil.boxedEquivalent(compType);
      }
      else
      {
        Class compType=(Class) iface.getTypeParameters()[0].getBounds()[0];
        arraySuffix.append(".list");
        iface=ClassUtil.boxedEquivalent(compType);
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
    { uriBuilder.append("java:/").append(iface.getName().replace('.','/'));
    }

    uriBuilder.append(arraySuffix.toString());    
    
    return URI.create(uriBuilder.toString());

  }
  
  /** 
   * Construct a ReflectionType which reflects 'clazz' and exposes itself
   *   as Tuple data.
   */
  public ReflectionType(TypeResolver resolver,URI typeUri,Class clazz)
  { 
    this.resolver=resolver;
    this.uri=typeUri;
    reflectedClass=clazz;
    nativeType=clazz;
    aggregate=checkAggregate(clazz);
  }
  
  
  /** 
   * Construct a ReflectionType which reflects 'clazz' and exposes itself
   *   as the specified nativeType.
   */
  public ReflectionType
    (TypeResolver resolver
    ,URI typeUri
    ,Class clazz
    ,Class nativeType
    )
  { 
    this.resolver=resolver;
    this.uri=typeUri;
    reflectedClass=clazz;
    this.nativeType=nativeType;
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

  public URI getUri()
  { return uri;
  }
  
  public TypeResolver getTypeResolver()
  { return resolver;
  }
  
  public Type getArchetype()
  { return archetype;
  }
  
  public boolean hasArchetype(Type type)
  {
    if (this==type)
    { return true;
    }
    else if (archetype!=null)
    { return archetype.hasArchetype(type);
    }
    else
    { return false;
    }
  }
  
  
  public Type getMetaType()
  {
    try
    { return resolver.resolve(ReflectionType.canonicalUri(getClass()));
    }
    catch (TypeNotFoundException x)
    { throw new RuntimeException(x);
    }
  }
  
  public Class getNativeClass()
  { return nativeType;
  }

  public boolean isPrimitive()
  { return false;
  }

  public boolean isAggregate()
  { return aggregate;
  }
  
  public Type getContentType()
  { return contentType;
  }

  public Type getCoreType()
  {
    Type ret=this;
    while (ret.isAggregate())
    { ret=ret.getContentType();
    }
    return ret;
  }
  
  public Scheme getScheme()
  { return scheme;
  }
  
  public ValidationResult validate(Object val)
  { 
    if (val!=null
        && !(nativeType.isAssignableFrom(val.getClass()))
       )
    { 
      return new ValidationResult
        (val.getClass().getName()
        +" cannot be assigned to "
        +nativeType.getClass().getName()
        );
    }
    else
    { return null;
    }
  }
  
  public void link()
    throws DataException
  {
    if (linked)
    { return;
    }
    linked=true;
    if (aggregate)
    { 
      Class contentClass;
      if (reflectedClass.isArray())
      { contentClass=reflectedClass.getComponentType();
      }
      else
      { 
        contentClass=
          (Class) reflectedClass.getTypeParameters()[0].getBounds()[0];
      }
      try
      { contentType=resolver.resolve(canonicalUri(contentClass));
      }
      catch (TypeNotFoundException x)
      { x.printStackTrace();
      }
        
    }

    if (reflectedClass.getSuperclass()!=null)
    { archetype=resolver.resolve(canonicalUri(reflectedClass.getSuperclass()));
    }
    
    scheme=new ReflectionScheme(resolver,this,reflectedClass);
    if (archetype!=null && archetype.getScheme()!=null)
    { scheme.setArchetypeScheme(archetype.getScheme());
    }
    scheme.resolve();
    
    classField=scheme.getFieldByName("class");
  }
  
  public boolean isStringEncodable()
  { return stringConstructor!=null;
  }
  
  public Object fromString(String val)
    throws DataException
  {
    if (nativeType==null)
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
    return null;
    
  }
  
  public String toString(Object val)
  {
    if (nativeType==null)
    { 
      throw new UnsupportedOperationException
        ("Type has no String representation");
    }
    
    if (val==null)
    { return null;
    }
    
    if (!nativeType.getClass().isAssignableFrom(val.getClass()))
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
  protected Object obtainInstance(Tuple tuple,InstanceResolver context)
    throws DataException
  {
    try
    {
      Class referencedClass=null;
      
      if (classField==null)
      { 
        System.err.println("No classField in "+getUri());
        referencedClass=nativeType;
      }
      else if (classField.getValue(tuple)!=null)
      { referencedClass=(Class) classField.getValue(tuple);
      }
      else
      { referencedClass=nativeType;
      }
     
      Object bean=null;
      
      if (context!=null)
      { bean=context.resolve(referencedClass);
      }
      
      if (bean==null)
      { bean=referencedClass.newInstance();
      }
      return bean;
    }
    catch (InstantiationException x)
    { throw new DataException("Error instantiating bean from Tuple '"+tuple+"':"+x.toString(),x);
    }
    catch (IllegalAccessException x)
    { throw new DataException("Error instantiating bean from Tuple '"+tuple+"':"+x.toString(),x);
    }
  }
  
  /**
   * Construct an Object using reflection to inject Bean properties.
   */
  public Object fromData(DataComposite val,InstanceResolver context)
    throws DataException
  {
    // System.err.println("--- ReflectionType.fromData\r\nDataComposite: "+val+"\r\n");
    if (nativeType==null)
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

    Object bean=obtainInstance(tuple,context);    
    scheme.depersistBeanProperties(tuple,bean);
    return bean;
  }

  
  public DataComposite toData(Object val)
    throws DataException
  {
    if (nativeType==null)
    { 
      throw new UnsupportedOperationException
        ("Type is already represented as a Tuple");
    }
    
    if (val==null)
    { return null;
    }

    if (!nativeType.isAssignableFrom(val.getClass()))
    { 
      throw new IllegalArgumentException
        (nativeType.getName()
        +" cannot be assigned a "
        +val.getClass().getName()
        );
    }
    
    EditableTuple tuple=new EditableArrayTuple(scheme);
    scheme.persistBeanProperties(val,tuple);
    return tuple;
      
  }
  
  public String toString()
  { return super.toString()+":"+uri.toString();
  }
}