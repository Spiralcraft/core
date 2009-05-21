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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.reflect.BeanReflector;

/**
 * Computes whether an Array contains a value
 * 
 * @author mike
 *
 * @param <T>
 */
public class ArrayContainsChannel<T>
  extends AbstractChannel<Boolean>
{
  
  private final Channel<T[]> arrayChannel;
  private final Channel<T> compareItemChannel;
  
  public ArrayContainsChannel
    (Channel<T[]> arrayChannel
    ,Channel<T> compareItemChannel
    )
  { 
    super(BeanReflector.<Boolean>getInstance(Boolean.class));
    this.arrayChannel=arrayChannel;
    this.compareItemChannel=compareItemChannel;
    
  }

  @Override
  protected Boolean retrieve()
  {
    T[] array=arrayChannel.get();
    if (array==null)
    { return null;
    }
    T compareItem=compareItemChannel.get();
    if (compareItem==null)
    { return null;
    }
    for (T item:array)
    { 
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
