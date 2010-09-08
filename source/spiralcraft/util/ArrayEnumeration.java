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
package spiralcraft.util;

import java.util.Enumeration;

/**
 * Enumerates through the elements of an Array
 */
public class ArrayEnumeration<T>
  implements Enumeration<T>
{
  private final T[] _array;
  private int _pos;

  public ArrayEnumeration(T[] array)
  { 
    _array=array;
    _pos=0;
  }

  @Override
  public boolean hasMoreElements()
  { return _pos<_array.length;
  }

  @Override
  public T nextElement()
  { return _array[_pos++];
  }

}
