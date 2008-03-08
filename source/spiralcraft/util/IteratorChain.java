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

public class IteratorChain<T>
  implements Iterator<T>
{
  
  private final Iterator<T>[] chain;
  private int pos;
  
  public IteratorChain(Iterator<T>... iterators)
  {
    this.chain=iterators;
    
  }

  @Override
  public boolean hasNext()
  { 
    if (pos<chain.length)
    {
      if (chain[pos].hasNext())
      { return true;
      }
      else
      { 
        pos++;
        return hasNext();
      }
    }
    else
    { return false;
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
