package spiralcraft.util;

import java.util.Enumeration;

/**
 * Enumerates through the elements of an Array
 */
public class ArrayEnumeration
  implements Enumeration
{
  private final Object[] _array;
  private int _pos;

  public ArrayEnumeration(Object[] array)
  { 
    _array=array;
    _pos=0;
  }

  public boolean hasMoreElements()
  { return _pos<_array.length;
  }

  public Object nextElement()
  { return _array[_pos++];
  }

}
