//
// Copyright (c) 2009,2010 Michael Toth
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
import java.util.LinkedList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;

public class ArraySelectChannel<X>
  extends SourcedChannel<X[],X[]>
{

  private final ThreadLocalChannel<X> componentChannel;
  private final Channel<Boolean> selector;
  
  /**
   * 
   * @param source A Channel which provides the source array
   * @param componentChannel The channel against which the selector is bound
   * @param selector The selector which evaluates the filter expression
   *  
   */
  public ArraySelectChannel
    (Channel<X[]> source
    ,ThreadLocalChannel<X> componentChannel
    ,Channel<Boolean> selector
    )
  { 
    super(source.getReflector(),source);
    this.componentChannel=componentChannel;
    this.selector=selector;
   
  }
    
    
  @SuppressWarnings("unchecked") // Array instantiation
  @Override
  protected X[] retrieve()
  {
    X[] array=source.get();
    if (array==null)
    { return null;
    }
    LinkedList<X> list=new LinkedList<X>();
    componentChannel.push(null);
    try
    {
      for (int i=0;i<array.length;i++)
      {
        componentChannel.set(array[i]);
        if (Boolean.TRUE.equals(selector.get()))
        { list.add(array[i]);
        }
      }
    }
    finally
    { componentChannel.pop();
    }
    
    X[] ret=(X[]) Array.newInstance
      (componentChannel.getReflector().getContentType()
      ,list.size()
      );
    
    return list.toArray(ret);
  }

  @Override
  protected boolean store(
    X[] val)
    throws AccessException
  { return false;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }

}
