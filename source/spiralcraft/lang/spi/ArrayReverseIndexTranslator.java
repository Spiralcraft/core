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
package spiralcraft.lang.spi;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.Translator;
import spiralcraft.util.ArrayUtil;

/**
 * <p>A Translator used as a means for accessing an array index. Translates
 *   an Array using a single subscript modifier to generate the value at
 *   the array subscript. This is unidirectional- does not allow the value
 *   to be updated.
 * </p>
 * @author mike
 *
 * @param <T>
 */
public class ArrayReverseIndexTranslator<T>
  implements Translator<Integer,T[]>
{
  private final Reflector<Integer> _reflector
    =BeanReflector.<Integer>getInstance(Integer.class);
  
  public ArrayReverseIndexTranslator()
  {
    
  }
  
  @Override
  public Reflector<Integer> getReflector()
  { return _reflector;
  }

  @Override
  public Integer translateForGet(T[] source,Channel<?>[] modifiers)
  { 
    if (modifiers==null || modifiers.length==0)
    { return null;
    }
    if (source==null)
    { return null;
    }
    return ArrayUtil.indexOf(source,modifiers[0].get());
  }

  @Override
  public T[] translateForSet(Integer value,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException("Array reverse index function is non-reversible");
  }

  /**
   * Arrays are mutable
   */
  @Override
  public boolean isFunction()
  { return false;
  }
}
