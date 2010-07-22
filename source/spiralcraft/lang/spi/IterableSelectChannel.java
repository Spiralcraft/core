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
package spiralcraft.lang.spi;

import java.util.LinkedList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;

/**
 * <p>Selects a subset of items from an Iterable using a boolean filter
 * </p>
 * 
 * @author mike
 *
 * @param <X>
 */
public class IterableSelectChannel<X>
  extends SourcedChannel<Iterable<X>,Iterable<X>>
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
  public IterableSelectChannel
    (Channel<Iterable<X>> source
    ,ThreadLocalChannel<X> componentChannel
    ,Channel<Boolean> selector
    )
  { 
    super(source.getReflector(),source);
    this.componentChannel=componentChannel;
    this.selector=selector;
   
  }
    
  @Override
  protected Iterable<X> retrieve()
  {
    Iterable<X> array=source.get();
    if (array==null)
    { return null;
    }
    LinkedList<X> list=new LinkedList<X>();
    componentChannel.push(null);
    try
    {
      for (X val:array)
      {
        componentChannel.set(val);
        if (selector.get())
        { list.add(val);
        }
      }
    }
    finally
    { componentChannel.pop();
    }
    
    return list;
  }

  @Override
  protected boolean store(
    Iterable<X> val)
    throws AccessException
  { return false;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }

}
