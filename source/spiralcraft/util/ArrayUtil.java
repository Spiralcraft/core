package spiralcraft.util;

/**
 * Static methods for array manipulation
 */
public class ArrayUtil
{


  /**
   * Format a String array into a String using the specified separator and delimiter.
   * No escape processing is performed by this method.
   */
  public static String formatToString(String[] array,String separator,String delimiter)
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
