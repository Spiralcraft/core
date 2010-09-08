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

import java.net.URI;
import java.util.Iterator;

import spiralcraft.lang.reflect.BeanReflector;

/**
 * Contains the state of an Iteration, which consists of the Iterator and
 *   the most recent value returned from Iterator.next()
 * 
 * @author mike
 *
 * @param <I>
 */
public class IterationCursor<I>
  implements Iterator<I>
{
  public static final URI FOCUS_URI
    =BeanReflector.<IterationCursor<?>>getInstance(IterationCursor.class)
      .getTypeURI();
    
  private int index=-1;
  private I value;
  private final Iterator<I> iterator;
  
  public IterationCursor(Iterator<I> iterator)
  { this.iterator=iterator;
  }
  
  public int getIndex()
  { return index;
  }
  
  public I getValue()
  { return value;
  }
  
  @Override
  public boolean hasNext()
  { 
    if (iterator!=null)
    { return iterator.hasNext();
    }
    else
    { return false;
    }
  }
  
  @Override
  public I next()
  {
    if (iterator!=null)
    { 
      value=iterator.next();
      index++;
    }
    return value;
  }
  
  @Override
  public void remove()
  { 
    if (iterator!=null)
    { iterator.remove();
    }
    value=null;
  }

}
