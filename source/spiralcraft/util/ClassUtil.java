package spiralcraft.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * Utilities for dealing with Classes and types
 */
public abstract class ClassUtil
{
  private static final Map<Class,Class> primitiveEquivalentMap =
    new HashMap<Class,Class>();

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

  private static final Map<Class,Class> boxedEquivalentMap =
    new HashMap<Class,Class>();

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

  private static final Map<Class,Set> primitiveCompatabilityMap =
    new HashMap<Class,Set>();

  static {
    Set set = new HashSet();

    set.add(Short.TYPE);
    set.add(Integer.TYPE);
    set.add(Long.TYPE);
    set.add(Float.TYPE);
    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Byte.TYPE, set);

    set = new HashSet();

    set.add(Integer.TYPE);
    set.add(Long.TYPE);
    set.add(Float.TYPE);
    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Short.TYPE, set);
    primitiveCompatabilityMap.put(Character.TYPE, set);

    set = new HashSet();

    set.add(Long.TYPE);
    set.add(Float.TYPE);
    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Integer.TYPE, set);

    set = new HashSet();

    set.add(Float.TYPE);
    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Long.TYPE, set);

    set = new HashSet();

    set.add(Double.TYPE);
    primitiveCompatabilityMap.put(Float.TYPE, set);
  }

  /**
   * Indicate whether the clazz represents a number
   */
  public static boolean isNumber(Class clazz)
  {
    return Number.class.isAssignableFrom(clazz)
      || Number.class.isAssignableFrom(boxedEquivalent(clazz));
  }
  
  /**
   * Indicates whether the formal class is assignable from the actual
   *   class, taking into account primitive promotion and auto-boxing
   *   semantics.
   */
  public static boolean isClassAssignableFrom(Class formal,Class actual)
  {
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
  public static Class primitiveEquivalent(Class clazz)
  { 
    return     
      clazz.isPrimitive()
      ?clazz
      :primitiveEquivalentMap.get(clazz)
      ;
  }    

  /**
   * Return the boxed equivalent of the specified Class
   */
  public static Class boxedEquivalent(Class clazz)
  { 
    return     
      clazz.isPrimitive()
      ?boxedEquivalentMap.get(clazz)
      :clazz;
  }    
}