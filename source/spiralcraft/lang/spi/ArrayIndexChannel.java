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

import java.lang.reflect.Array;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

public class ArrayIndexChannel<T>
  extends AbstractChannel<T>
{
  
  private final Channel<T[]> arrayChannel;
  private final Channel<Number> subscriptChannel;
  private final boolean primitive;
  
  public ArrayIndexChannel
    (Reflector<T> componentReflector
    ,Channel<T[]> arrayChannel
    ,Channel<Number> subscriptChannel
    )
  { 
    super(componentReflector);
    this.arrayChannel=arrayChannel;
    this.subscriptChannel=subscriptChannel;
    this.primitive
      =arrayChannel.getReflector().getContentType().getComponentType()
        .isPrimitive();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected T retrieve()
  {
    if (!primitive)
    {
      T[] array=arrayChannel.get();
      if (array==null)
      { return null;
      }
      Number subscript=subscriptChannel.get();
      if (subscript==null)
      { return null;
      }
      return array[subscript.intValue()];
    }
    else
    { 
      Object array=arrayChannel.get();
      if (array==null)
      { return null;
      }
      Number subscript=subscriptChannel.get();
      if (subscript==null)
      { return null;
      }
      return (T) Array.get(array,subscript.intValue());
    }
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  {
    T[] array=arrayChannel.get();
    if (array==null)
    { return false;
    }
    Number subscript=subscriptChannel.get();
    if (subscript==null)
    { return false;
    }
    array[subscript.intValue()]=val;
    return true;
  }
  
  @Override
  public boolean isWritable()
  { return true;
  }

}
