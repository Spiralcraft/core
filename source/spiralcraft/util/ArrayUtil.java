package spiralcraft.util;

import java.lang.reflect.Array;

import java.util.TreeSet;
import java.util.Collection;

/**
 * Static methods for array manipulation
 */
public class ArrayUtil
{

  /**
   * Append a value to an array
   */
  public static Object append(Object array,Object value)
  { 
    array=expandBy(array,1);
    Array.set(array,Array.getLength(array)-1,value);
    return array;
  }

  /**
   * Prepend a value to an array
   */
  public static Object prepend(Object array,Object value)
  { 
    array=expandBy(array,1);
    Array.set(array,0,value);
    return array;
  }

  /**
   * Append an array to an array
   */
  public static Object appendArrays(Object array1,Object array2)
  { 
    int appendPoint=Array.getLength(array1);
    int appendElements=Array.getLength(array2);
    array1=expandBy(array1,appendElements);
    System.arraycopy(array2, 0, array1, appendPoint, appendElements);
    return array1;
  }

  /**
   * Merge the contents of two arrays, discarding duplicate entries
   */
  public static Object mergeArrays(Object array1,Object array2)
  { 
    TreeSet treeSet=new TreeSet();
    addToCollection(treeSet,array1);
    addToCollection(treeSet,array2);
    Object array3=Array.newInstance(array1.getClass().getComponentType(),treeSet.size());
    Object[] result=treeSet.toArray();
    System.arraycopy(result, 0, array3, 0, result.length);
    return array3;
  }

  /**
   * Add the contents of the array to a collection
   *@returns The number of elements successfully added
   */
  public static int addToCollection(Collection c,Object array)
  {
    int length=Array.getLength(array);
    int count=0;
    for (int i=0;i<length;i++)
    { 
      if (c.add(Array.get(array,i)))
      { count++;
      }
    }
    return count;
  }

  /**
   * Find an object in a target array using the equals() method and
   *   return the found objects.
   */
  public static Object find(Object array,Object target)
  {
    int index=indexOf(array,target);
    if (index>-1)
    { return Array.get(array,index);
    }
    return null;
  }

  /**
   * Find an object in a target array using the equals() method and
   *   return the array index where it was found
   */
  public static int indexOf(Object array,final Object target)
  { 
    int length=Array.getLength(array);
    for (int i=0;i<length;i++)
    { 
      Object val=Array.get(array,i);
      if (val==target)
      { return i;
      }
      if (target!=null && target.equals(val))
      { return i;
      }
    }
    return -1;
  }

  public static boolean contains(Object array,final Object target)
  { return indexOf(array,target)>=0;
  }

  /**
   * Expand an array by adding the specified number of elements
   */
  public static Object expandBy(Object array,int expandBy)
  {
    Object newArray = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array)+expandBy);
    System.arraycopy(array, 0, newArray, 0, Array.getLength(array));
    return newArray;
  }

  public static Object remove(Object array,Object val)
  {
    int index=indexOf(array,val);
    if (index==-1)
    { return array;
    }
    Object newArray = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array)-1);
    System.arraycopy(array,0,newArray,0,index);
    System.arraycopy(array,index+1,newArray,index,Array.getLength(newArray)-index);
    return newArray;
  }

  /**
   * Truncate an array by removing the specified number of elements from
   *   the head of the array.
   */
  public static Object truncateBefore(Object array,int numElements)
  {
    Object newArray = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array)-numElements);
    System.arraycopy(array, numElements, newArray, 0, Array.getLength(newArray));
    return newArray;
  }
  
  /**
   * Truncate an array to the specified number of elements.
   */
  public static Object truncate(Object array,int numElements)
  {
    Object newArray = Array.newInstance(array.getClass().getComponentType(), numElements);
    System.arraycopy(array, 0, newArray, 0, numElements);
    return newArray;
  }

  /**
   * Format a String array into a String using the specified separator and delimiter.
   * No escape processing is performed by this method.
   */
  public static String formatToString(Object[] array,String separator,String delimiter)
  {
    StringBuffer buf=new StringBuffer();
    for (int i=0;i<array.length;i++)
    { 
      if (i>0)
      { buf.append(separator);
      }
      if (array[i]!=null)
      { 
        if (delimiter!=null)
        { buf.append(delimiter).append(array[i]).append(delimiter);
        }
        else
        { buf.append(array[i]);
        }
      }
    }
    return buf.toString();
    
  }
}
