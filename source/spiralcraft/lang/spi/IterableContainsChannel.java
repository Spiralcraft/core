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
package spiralcraft.lang.spi;

import java.util.Iterator;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.reflect.BeanReflector;

/**
 * Computes whether an Array contains a value
 * 
 * @author mike
 *
 * @param <T>
 */
public class IterableContainsChannel<C,T>
  extends SourcedChannel<C,Boolean>
{
  
  private final Channel<T> compareItemChannel;
  private final IterationDecorator<C,T> decorator;
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public IterableContainsChannel
    (Channel<C> iterableChannel
    ,Channel<T> compareItemChannel
    )
    throws BindException
  { 
    super(BeanReflector.<Boolean>getInstance(Boolean.class),iterableChannel);
    this.compareItemChannel=compareItemChannel;
    this.decorator
      = iterableChannel.<IterationDecorator>decorate(IterationDecorator.class);
    
  }

  @Override
  protected Boolean retrieve()
  {
    Iterator<T> it = decorator.iterator();
    
    T compareItem=compareItemChannel.get();

    if (it==null)
    { return null;
    }
    if (compareItem==null)
    { return null;
    }
    
    while (it.hasNext())
    { 
      T item=it.next();
      if (compareItem.equals(item))
      { return true;
      }
    }
    return false;
    
  }

  @Override
  protected boolean store(
    Boolean val)
    throws AccessException
  { return false;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }

}
