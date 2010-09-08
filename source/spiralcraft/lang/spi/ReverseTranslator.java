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
package spiralcraft.lang.spi;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

/**
 * <p>Reverses the direction of an existing Translator
 * </p>
 * 
 * @author mike
 *
 */
public class ReverseTranslator<Tderived, Torigin>
  implements Translator<Tderived, Torigin>
{
  private final Reflector<Tderived> result;
  private final Translator<Torigin,Tderived> delegate;
  
  public ReverseTranslator
    (Reflector<Tderived> result
    ,Translator<Torigin,Tderived> delegate
    )
  {
    this.result=result;
    this.delegate=delegate;
  }

  @Override
  public Reflector<Tderived> getReflector()
  { return result;
  }

  @Override
  public Tderived translateForGet(
    Torigin source,
    Channel<?>[] modifiers)
  { return delegate.translateForSet(source,modifiers);
  }

  @Override
  public Torigin translateForSet(
    Tderived source,
    Channel<?>[] modifiers)
  { return delegate.translateForGet(source,modifiers);
  }
  
  /**
   * Can't assume that the reverse of a function is still a function
   */
  @Override
  public boolean isFunction()
  { return false;
  }
}
