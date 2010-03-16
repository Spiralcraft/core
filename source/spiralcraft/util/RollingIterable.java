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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>A synchronized Iterable that allows for simultaneous iteration and 
 *   addition such that an in-process Iterator will pick up new items added
 *   before Iterator.hasNext() returns false.
 * </p>
 * 
 * @author mike
 *
 */
public class RollingIterable<T>
  implements Iterable<T>
{

  private final ArrayList<T> list
    =new ArrayList<T>();

  
  public synchronized void add(T item)
  { list.add(item);
  }
  
  public Iterator<T> iterator()
  { return new Itr();
  }
  
  class Itr
    implements Iterator<T>
  {
    int pos;
    
    public boolean hasNext()
    {
      synchronized (RollingIterable.this)
      { return pos<list.size();
      }
    }
    
    public T next()
    {
      synchronized (RollingIterable.this)
      { 
        if (pos<list.size())
        { return list.get(pos++);
        }
        else
        { throw new NoSuchElementException(""+pos);
        }
      }
    }
    
    public void remove()
    { 
      throw new UnsupportedOperationException
        ("RollingIterable iterator does not support removeal");
    }
  }
}
