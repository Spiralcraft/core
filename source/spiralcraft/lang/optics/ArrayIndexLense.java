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
package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;

import spiralcraft.lang.optics.Lense;
import spiralcraft.lang.optics.Prism;

public class ArrayIndexLense<T>
  implements Lense<T,T[]>
{
  private final Prism<T> _prism;
  
  public ArrayIndexLense(Prism<T> prism)
  { _prism=prism;
  }
  
  public Prism<T> getPrism()
  { return _prism;
  }

  @SuppressWarnings("unchecked") // Upcast for expected modifiers
  public T translateForGet(T[] source,Optic[] modifiers)
  { 
    Number index=((Optic<Number>)modifiers[0]).get();
    if (index==null)
    { return null;
    }
    if (source==null)
    { return null;
    }
    return source[index.intValue()];
  }

  @SuppressWarnings("unchecked") // Upcast for expected modifiers
  public T[] translateForSet(T value,Optic[] modifiers)
  { throw new UnsupportedOperationException("Can't reverse array index");
  }


}
