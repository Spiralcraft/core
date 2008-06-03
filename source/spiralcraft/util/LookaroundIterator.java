//
// Copyright (c) 1998,2008 Michael Toth
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

/**
 * <P>An Iterator which wraps another Iterator and provides access to both the
 *   pending item that will be returned in the next call to "next" as well as
 *   the last item returned, without advancing the Iterator.  Does not support
 *    "remove".
 * </P>
 * 
 * <P>Primarily designed to facilitate operations that detect if properties
 *   of the item "stream" are about to change.
 * </P>
 *   
 * 
 * @author mike
 *
 */
public class LookaroundIterator<T>
  implements Iterator<T>
{

  private final Iterator<T> delegate;
  
  private T next;
  private boolean hasNext;
  
  private T previous;
  private boolean hasPrevious;
  
  public LookaroundIterator(Iterator<T> delegate)
  {
    this.delegate=delegate;
    hasNext=delegate.hasNext();
    if (hasNext)
    { next=delegate.next();
    }
  }
  
  
  @Override
  public boolean hasNext()
  { return hasNext;
  }

  @Override
  public T next()
  {
    T ret=next;
    
    hasPrevious=true;
    previous=next;
    
    hasNext=delegate.hasNext();
    if (hasNext)
    { next=delegate.next();
    }
    else
    { next=null;
    }
      
    return ret;
  }

  /**
   * @return Whether next() has been called yet
   */
  public boolean hasPrevious()
  { return hasPrevious;
  }
  
  
  /**
   * @return The value last returned by "next()"
   */
  public T getPrevious()
  { return previous;
  }
  
  /**
   * 
   * @return The value that will be returned when "next()" is called
   */
  public T getCurrent()
  { return next;
  }

  @Override
  public void remove()
  { throw new UnsupportedOperationException
      ("Lookahead Iterator does not support remove()");
  }


}
