package spiralcraft.util;

import java.lang.reflect.Array;

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
   * Expand an array by adding the specified number of elements
   */
  public static Object expandBy(Object array,int expandBy)
  {
    Object newArray = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array)+expandBy);
    System.arraycopy(array, 0, newArray, 0, Array.getLength(array));
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
