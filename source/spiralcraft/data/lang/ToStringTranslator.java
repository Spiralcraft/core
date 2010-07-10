//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.data.lang;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.Translator;

/**
 * <p>A Translator which calls the toString() and fromString() methods on 
 *   a dataType to externalize/internalize an object
 * </p>
 *   
 * 
 * @author mike
 *
 * @param <T>
 */
public class ToStringTranslator<T>
  implements Translator<String, T>
{

  
  private final Reflector<String> reflector;
  private final Type<T> type;
  
  public ToStringTranslator(Type<T> type)
    throws DataException
  { 
    this.type=type;
    reflector=BeanReflector.getInstance(String.class);
  }
  
  @Override
  public Reflector<String> getReflector()
  { return reflector;
  }

  @Override
  public String translateForGet(
    T source,
    Channel<?>[] modifiers)
  { 
    return type.toString(source);
  }

  @Override
  public T translateForSet(
    String source,
    Channel<?>[] modifiers)
  { 
    try
    { return type.fromString(source);
    }
    catch (DataException x)
    { throw new AccessException("Error internalizing "+source);
    }
  } 
  
  public boolean isFunction()
  { return false;
  }
}
