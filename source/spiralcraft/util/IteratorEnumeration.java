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
public class IteratorEnumeration
  implements Enumeration
{

  private final Iterator _iterator;

  public IteratorEnumeration(Iterator iterator)
  { _iterator=iterator;
  }    

  public boolean hasMoreElements()
  { return _iterator.hasNext();
  }

  public Object nextElement()
  { return _iterator.next();
  }
}
