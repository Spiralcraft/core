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
package spiralcraft.util.lang;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

/**
 * Utilities for dealing with Classes and types
 */
public abstract class ClassUtil
{
  private static final Map<Class<?>,Class<?>> primitiveEquivalentMap =
    new HashMap<Class<?>,Class<?>>();

  static {
    primitiveEquivalentMap.put(Boolean.class, Boolean.TYPE);
    primitiveEquivalentMap.put(Byte.class, Byte.TYPE);
    primitiveEquivalentMap.put(Character.class, Character.TYPE);
    primitiveEquivalentMap.put(Double.class, Double.TYPE);
    primitiveEquivalentMap.put(Float.class, Float.TYPE);
    primitiveEquivalentMap.put(Integer.class, Integer.TYPE);
    primitiveEquivalentMap.put(Long.class, Long.TYPE);
    primitiveEquivalentMap.put(Short.class, Short.TYPE);
    primitiveEquivalentMap.put(Void.class, Void.TYPE);
  }

  private static final Map<Class<?>,Class<?>> boxedEquivalentMap =
    new HashMap<Class<?>,Class<?>>();

  static {
    boxedEquivalentMap.put(Boolean.TYPE, Boolean.class);
    boxedEquivalentMap.put(Byte.TYPE, Byte.class);
    boxedEquivalentMap.put(Character.TYPE, Character.class);
    boxedEquivalentMap.put(Double.TYPE, Double.class);
    boxedEquivalentMap.put(Float.TYPE, Float.class);
    boxedEquivalentMap.put(Integer.TYPE, Integer.class);
    boxedEquivalentMap.put(Long.TYPE, Long.class);
    boxedEquivalentMap.put(Short.TYPE, Short.class);
    boxedEquivalentMap.put(Void.TYPE, Void.class);
  }

  private static final Map<Class<?>,Set<?>> primitiveCompatabilityMap =
    new HashMap<Class<?>,Set<?>>();

  static {
    Set<Class<?>> set = new HashSet<Class<?>>();

    set.add(Short.TYPE);
    set.add(Integer.TYPE);
    set.add(Long.TYPE);
    set.add(Float.TYPE);
    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Byte.TYPE, set);

    set = new HashSet<Class<?>>();

    set.add(Integer.TYPE);
    set.add(Long.TYPE);
    set.add(Float.TYPE);
    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Short.TYPE, set);
    primitiveCompatabilityMap.put(Character.TYPE, set);

    set = new HashSet<Class<?>>();

    set.add(Long.TYPE);
    set.add(Float.TYPE);
    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Integer.TYPE, set);

    set = new HashSet<Class<?>>();

    set.add(Float.TYPE);
    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Long.TYPE, set);

    set = new HashSet<Class<?>>();

    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Float.TYPE, set);
  }

  /**
   * Indicate whether the clazz represents a number
   */
  public static boolean isNumber(Class<?> clazz)
  {
    return Number.class.isAssignableFrom(clazz)
      || Number.class.isAssignableFrom(boxedEquivalent(clazz));
  }
  
  /**
   * Indicates whether the formal class is assignable from the actual
   *   class, taking into account primitive promotion and auto-boxing
   *   semantics.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static boolean isClassAssignableFrom(Class formal,Class actual)
  {
    // Suppress generic warnings b/c we're in meta-land here
    
    if (formal==actual)
    { return true;
    }
    
    if (formal.isAssignableFrom(actual))
    { return true;
    }
    
    if (formal==Object.class)
    { return true;
    }

    if (actual==Void.TYPE)
    { return true;
    }
    
    
    Class actualPrimitive = primitiveEquivalent(actual);
    if (actualPrimitive==null)
    { return false;
    }

    Class formalPrimitive = primitiveEquivalent(formal);
    if (formalPrimitive==null)
    { return false;
    }

    if (actualPrimitive==formalPrimitive)
    { return true;
    }
    
    Set compatibleSet = primitiveCompatabilityMap.get(actualPrimitive);
    if (compatibleSet==null)
    { return false;
    }
    
    return compatibleSet.contains(formalPrimitive);
  }
  
  /**
   * Return the primitive equivalent of the specified Class
   */
  public static Class<?> primitiveEquivalent(Class<?> clazz)
  { 
    return     
      clazz.isPrimitive()
      ?clazz
      :primitiveEquivalentMap.get(clazz)
      ;
  }    

  /**
   * Return the primitive default value for the specified primitive Class
   */
  public static Object primitiveDefault(Class<?> clazz)
  { 
    if (clazz==Boolean.TYPE)
    { return Boolean.FALSE;
    }
    else if (clazz==Byte.TYPE)
    { return 0;
    }
    else if (clazz==Short.TYPE)
    { return 0;
    }
    else if (clazz==Integer.TYPE)
    { return 0;
    }
    else if (clazz==Long.TYPE)
    { return 0;
    }
    else if (clazz==Float.TYPE)
    { return 0;
    }
    else if (clazz==Double.TYPE)
    { return 0;
    }
    else if (clazz==Character.TYPE)
    { return '\0';
    }
    else
    { return null;
    }
  }    

  /**
   * Return the boxed equivalent of the specified Class
   */
  public static Class<?> boxedEquivalent(Class<?> clazz)
  { 
    return     
      clazz.isPrimitive()
      ?boxedEquivalentMap.get(clazz)
      :clazz;
  }    
 
  /**
   * @return The Constructor for the specified class that is the
   *   closest match to the specified signature, taking into account
   *   primitive boxing.
   */
  public static Constructor<?> getConstructor
    (Class<?> clazz
    ,Class<?>[] signature
    )
  { 
    try
    { return new MethodResolver(clazz).findConstructor(signature);
    }
    catch (NoSuchMethodException x)
    { return null;
    }
  }

  /**
   * @return The Constructor for the specified class that is the
   *   closest match to the specified signature, taking into account
   *   primitive boxing.
   */
  public static Method getMethod
    (Class<?> clazz
    ,String name
    ,Class<?>[] signature
    )
  { 
    try
    { return new MethodResolver(clazz).findMethod(name,signature);
    }
    catch (NoSuchMethodException x)
    { return null;
    }
  }

  /**
   * Get the underlying class for a type,
   *  or null if the type is a variable type.
   * 
   * @param type the type
   * @return the underlying class
   */
  @SuppressWarnings("rawtypes")
  public static Class<?> getClass(Type type) 
  {
    if (type instanceof Class) 
    { return (Class) type;
    }
    else if (type instanceof ParameterizedType) 
    { return getClass(((ParameterizedType) type).getRawType());
    }
    else if (type instanceof GenericArrayType) 
    {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      Class<?> componentClass = getClass(componentType);
      if (componentClass != null ) {
        return Array.newInstance(componentClass, 0).getClass();
      }
      else {
        return Object[].class;
      }
    }
    else {
      return null;
    }
  }   
  
  /**
   * <p>The type that a class binds a type parameter of one of its generic
   *   supertypes to, found through its superclasses and interfaces, with
   *   each level's arguments substituted for the type variables of the
   *   level above.
   * </p>
   *
   * <p>Null when the parameter is not bound to a type the class declares
   *   (it stays a type variable), or when generic is not a supertype of
   *   type. A parameterized argument is given as declared: type variables
   *   nested in it are not substituted, and its raw class (getClass(Type))
   *   is right.
   * </p>
   *
   * @param type the class, such as spiralcraft.json.ObjectNode
   * @param generic the generic supertype, such as AbstractNode
   * @param index the position of the type parameter in generic
   * @throws IllegalArgumentException for an index generic does not have
   */
  public static Type resolveTypeArgument
    (Class<?> type,Class<?> generic,int index)
  {
    TypeVariable<?>[] parameters=generic.getTypeParameters();
    if (index<0 || index>=parameters.length)
    {
      throw new IllegalArgumentException
        (generic.getName()+" has no type parameter at "+index);
    }
    Map<TypeVariable<?>,Type> bindings
      =bindingsTo(type,generic,new HashMap<TypeVariable<?>,Type>());
    if (bindings==null)
    { return null;
    }
    Type argument=bindings.get(parameters[index]);
    return argument instanceof TypeVariable?null:argument;
  }

  /**
   * The type variables bound on the way from type up to generic, or null
   *   if generic is not reached
   */
  private static Map<TypeVariable<?>,Type> bindingsTo
    (Class<?> type,Class<?> generic,Map<TypeVariable<?>,Type> bindings)
  {
    if (type==generic)
    { return bindings;
    }
    List<Type> supertypes=new ArrayList<Type>();
    if (type.getGenericSuperclass()!=null)
    { supertypes.add(type.getGenericSuperclass());
    }
    for (Type supertype:type.getGenericInterfaces())
    { supertypes.add(supertype);
    }
    for (Type supertype:supertypes)
    {
      Class<?> raw=getClass(supertype);
      if (raw==null || !generic.isAssignableFrom(raw))
      { continue;
      }
      Map<TypeVariable<?>,Type> next
        =new HashMap<TypeVariable<?>,Type>(bindings);
      if (supertype instanceof ParameterizedType)
      {
        TypeVariable<?>[] variables=raw.getTypeParameters();
        Type[] arguments
          =((ParameterizedType) supertype).getActualTypeArguments();
        for (int i=0;i<variables.length;i++)
        {
          Type argument=arguments[i];
          if (argument instanceof TypeVariable
              && bindings.containsKey(argument)
             )
          { argument=bindings.get(argument);
          }
          next.put(variables[i],argument);
        }
      }
      Map<TypeVariable<?>,Type> found=bindingsTo(raw,generic,next);
      if (found!=null)
      { return found;
      }
    }
    return null;
  }

  public static Class<?> getTypeArgumentAsClass(Type type)
  {
    if (type instanceof ParameterizedType)
    {
      Type[] args=((ParameterizedType) type).getActualTypeArguments();
      if (args.length>=1)
      { return getClass(args[0]);
      }
    }
    return null;
  }

  public static final boolean equals(Object a,Object b)
  { return (a==b || (a!=null && a.equals(b)));
  }
  
  /**
   * Construct a new instance of a class using the default constructor, wrapping
   *   any checked exceptions into an InvocationTargetException
   * 
   * @param clazz
   * @return
   */
  public static final <T> T construct(Class<T> clazz)
    throws InvocationTargetException
  {
    try
    { return clazz.getDeclaredConstructor().newInstance();
    }
    catch (NoSuchMethodException x)
    {          
      throw new InvocationTargetException
        (x
        ,"Class "
        +clazz.getName()
        +" could not be instantiated: "
        +x
        );
    }
    catch (InstantiationException x)
    {
      throw new InvocationTargetException
        (x
        ,"Class "
        +clazz.getName()
        +" could not be instantiated: "
        +x
        );
    }
    catch (IllegalAccessException x)
    {
      throw new InvocationTargetException
        (x
        ,"Class "
        +clazz.getName()
        +" could not be instantiated: "
        +x
        );
    }
    catch (InvocationTargetException x)
    {
      throw new InvocationTargetException
        (x
        ,"Class "
        +clazz.getName()
        +" could not be instantiated: "
        +x
        );
    }
    
  }
}