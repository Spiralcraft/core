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
import java.util.Enumeration;

/**
 * Provides an Iterator interface to an Enumeration
 */
public class EnumerationIterator<T>
  implements Iterator<T>
{

  private final Enumeration<T> _enumeration;

  public EnumerationIterator(Enumeration<T> enumeration)
  { _enumeration=enumeration;
  }    

  @Override
  public boolean hasNext()
  { return _enumeration.hasMoreElements();
  }

  @Override
  public T next()
  { return _enumeration.nextElement();
  }
  
  @Override
  public void remove()
  { throw new UnsupportedOperationException
      ("Cannot remove: backed by an Enumeration");
  }
}
