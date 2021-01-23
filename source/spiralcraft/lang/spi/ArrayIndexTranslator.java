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
public class ArrayIndexTranslator<T>
  implements Translator<T,T[]>
{
  private final Reflector<T> _reflector;
  
  public ArrayIndexTranslator(Reflector<T> reflector)
  { _reflector=reflector;
  }
  
  @Override
  public Reflector<T> getReflector()
  { return _reflector;
  }

  @Override
  @SuppressWarnings("unchecked") // Upcast for expected modifiers
  public T translateForGet(T[] source,Channel<?>[] modifiers)
  { 
    if (modifiers==null || modifiers.length==0)
    { return null;
    }
    Number index=((Channel<Number>)modifiers[0]).get();
    if (index==null)
    { return null;
    }
    if (source==null)
    { return null;
    }
    return source[index.intValue()];
  }

  @Override
  public T[] translateForSet(T value,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException("Can't reverse array index");
  }

  /**
   * Arrays are mutable
   */
  @Override
  public boolean isFunction()
  { return false;
  }
}
