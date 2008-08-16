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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

import spiralcraft.lang.spi.Translator;

/**
 * <p>A Translator used to compare arrays.
 * </p>
 * @author mike
 *
 * @param <T>
 */
public abstract class ArrayEqualityTranslator<T>
  implements Translator<Boolean,T>
{
  private final Reflector<Boolean> _reflector;
  
  public ArrayEqualityTranslator()
  { 
    try
    { _reflector=BeanReflector.getInstance(Boolean.class);
    }
    catch (BindException x)
    { throw new RuntimeException("Couldn't bind boolean");
    }
  }
  
  public Reflector<Boolean> getReflector()
  { return _reflector;
  }

  @SuppressWarnings("unchecked") // Upcast for expected modifiers
  public Boolean translateForGet(T source,Channel<?>[] modifiers)
  { 
    if (modifiers==null || modifiers.length==0)
    { return null;
    }
    T aCompare=(T) modifiers[0].get();
    return source==null ? aCompare==null : compare(source,aCompare);
  }

  public T translateForSet(Boolean value,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException("Can't reverse array equality");
  }

  public abstract boolean compare(T source,T target);

}
