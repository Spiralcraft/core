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

/**
 * <p>An Iterator which generates a concatenation of elements from a list of
 *   Iterators. 
 * </p>
 * 
 * <p>At any time before the end of the Iteration (the point when hasNext() 
 *   returns false), an additional iterator can be queued at the end.
 * </p>
 *   
 * @author mike
 *
 * @param <T>
 */
public class IteratorChain<T>
  implements Iterator<T>
{
  
  private Iterator<? extends T>[] chain;
  private int pos;
  private boolean done;
  
  @SuppressWarnings("unchecked")
  public IteratorChain(Iterator<? extends T>... iterators)
  { this.chain=iterators;
  }

  public void queue(Iterator<T> last)
  { 
    if (!done)
    { chain=ArrayUtil.append(chain,last);
    }
    else
    { 
      throw new IllegalStateException
        ("Cannot queue an Iterator into an completed IteratorChain");
    }
  }
  
  @Override
  public boolean hasNext()
  { 
    if (done)
    { return false;
    }
    
    if (pos<chain.length)
    {
      if (chain[pos].hasNext())
      { return true;
      }
      else
      { 
        pos++;
        if (hasNext())
        { return true;
        }
        else
        { 
          done=true;
          return false;
        }
      }
    }
    else
    { 
      done=true;
      return false;
    }
    
  }
  

  @Override
  public T next()
  {
    if (hasNext())
    { return chain[pos].next();
    }
    else
    { return null;
    }
  }

  @Override
  public void remove()
  {
    if (hasNext())
    { chain[pos].remove();
    }    
  }

}
