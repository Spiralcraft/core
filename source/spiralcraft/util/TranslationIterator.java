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

import java.util.Iterator;

public abstract class TranslationIterator<Tsource,Tresult>
  implements Iterator<Tresult>
{
  private final Iterator<Tsource> iterator;
  
  public TranslationIterator(Iterator<Tsource> iterator)
  { this.iterator=iterator;
  }
  
  @Override
  public final boolean hasNext()
  { return iterator.hasNext();
  }

  @Override
  public final Tresult next()
  { return translate(iterator.next());
  }

  @Override
  public final void remove()
  { iterator.remove();
  }

  protected abstract Tresult translate(Tsource val);
}
