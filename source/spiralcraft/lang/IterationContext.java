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
package spiralcraft.lang;

import java.util.Iterator;

/**
 * Contains the state of an Iteration, which consists of the Iterator and
 *   the most recent value returned from Iterator.next()
 * 
 * @author mike
 *
 * @param <I>
 */
public class IterationContext<I>
  implements Iterator
{
  private I value;
  private final Iterator<I> iterator;
  
  public IterationContext(Iterator<I> iterator)
  { this.iterator=iterator;
  }
  
  public I getValue()
  { return value;
  }
  
  public boolean hasNext()
  { return iterator.hasNext();
  }
  
  public I next()
  {
    value=iterator.next();
    return value;
  }
  
  public void remove()
  { 
    iterator.remove();
    value=null;
  }

}
