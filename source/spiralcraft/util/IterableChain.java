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
package spiralcraft.util;

import java.util.Iterator;

/**
 * Concatenates a number of Uterables into a single Iterable
 * 
 * @author mike
 *
 * @param <T>
 */
public class IterableChain<T>
  implements Iterable<T>
{
  
  private final Iterable<T>[] chain;
  
  public IterableChain(Iterable<T>... iterables)
  { this.chain=iterables;
  }
  
  @Override
  public Iterator<T> iterator()
  { return new ChainIterator();
  }

  class ChainIterator
    implements Iterator<T>
  {
    private volatile int pos=0;
    private volatile Iterator<T> current;
    
    @Override
    public boolean hasNext()
    { 
      if (current!=null && current.hasNext())
      { return true;
      }
      else if (pos<chain.length)
      {
        current=chain[pos++].iterator();
        return hasNext();
      }
      else
      { 
        current=null;
        return false;
      }

    }


    @Override
    public T next()
    {
      if (hasNext())
      { return current.next();
      }
      else
      { return null;
      }
    }

    @Override
    public void remove()
    {
      if (hasNext())
      { current.remove();
      }    
    }
  }

}
