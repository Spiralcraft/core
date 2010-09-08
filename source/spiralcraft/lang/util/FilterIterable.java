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
package spiralcraft.lang.util;


import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implements an IterationDecorator for a source that returns an Iterator
 */
public class FilterIterable<I>
  implements Iterable<I>
{
  private final Iterable<I> source;
  private final Channel<Boolean> filter;
  private final ThreadLocalChannel<I> item;
  private final boolean reverse;
  
  public FilterIterable
    (Iterable<I> source
    ,Reflector<I> componentReflector
    ,Focus<?> context
    ,Expression<Boolean> filterX
    )
    throws BindException
  { 
    this.source=source;
    item=new ThreadLocalChannel<I>(componentReflector);
    item.setContext(context);
    filter=context.bind(filterX);
    this.reverse=false;
  }

  public FilterIterable
    (Iterable<I> source
    ,ThreadLocalChannel<I> item
    ,Channel<Boolean> filter
    ,boolean reverse
    )
    throws BindException
  { 
    this.source=source;
    this.item=item;
    this.filter=filter;
    this.reverse=reverse;
  }
  
  @Override
  public Iterator<I> iterator()
  { return new FilterIterator(source.iterator());
  }

  class FilterIterator
    implements Iterator<I>
  {
    private I next;
    private final Iterator<I> iterator;
    
    public FilterIterator(Iterator<I> iterator)
    { this.iterator=iterator;
    }
    
    @Override
    public boolean hasNext()
    { 
      if (next!=null)
      { return true;
      }
      else
      {
        while (next==null && iterator.hasNext()) 
        { 
          if (filter==null)
          { 
            next=iterator.next();
            return true;
          }
          else
          {
            I test=iterator.next();
            item.push(test);
            try
            {
              if (Boolean.TRUE.equals(filter.get()))
              { 
                if (!reverse)
                {
                  next=test;
                  return true;
                }
              }
              else
              {
                if (reverse)
                { 
                  next=test;
                  return true;
                }
              }
            }
            finally
            { item.pop();
            }
          }
        }
      }
      return false;
    }

    @Override
    public I next()
    { 
      if (hasNext())
      { 
        try
        { return next;
        }
        finally
        { next=null;
        }
      }
      else
      { throw new NoSuchElementException();
      }
    }

    @Override
    public void remove()
    { iterator.remove();
    }
  }
  
}
