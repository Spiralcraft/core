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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;

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
  @SuppressWarnings("unchecked")
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
  @SuppressWarnings("unchecked")
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
        return null;
      }
    }
    else {
      return null;
    }
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

}