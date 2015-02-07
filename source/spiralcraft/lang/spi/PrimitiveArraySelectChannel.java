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

public class PrimitiveArraySelectChannel<Tarray,Titem>
  extends SourcedChannel<Tarray,Tarray>
{

  private final ThreadLocalChannel<Titem> componentChannel;
  private final Channel<Boolean> selector;
  
  /**
   * 
   * @param source A Channel which provides the source array
   * @param componentChannel The channel against which the selector is bound
   * @param selector The selector which evaluates the filter expression
   *  
   */
  public PrimitiveArraySelectChannel
    (Channel<Tarray> source
    ,ThreadLocalChannel<Titem> componentChannel
    ,Channel<Boolean> selector
    )
  { 
    super(source.getReflector(),source);
    this.componentChannel=componentChannel;
    this.selector=selector;
   
  }
    
    
  @SuppressWarnings("unchecked") // Array instantiation
  @Override
  protected Tarray retrieve()
  {
    Tarray array=source.get();
    if (array==null)
    { return null;
    }
    LinkedList<Titem> list=new LinkedList<Titem>();
    componentChannel.push(null);
    try
    {
      int l=Array.getLength(array);
      for (int i=0;i<l;i++)
      {
        Titem item=(Titem) Array.get(array,i);
        componentChannel.set(item);
        if (Boolean.TRUE.equals(selector.get()))
        { list.add(item);
        }
      }
    }
    finally
    { componentChannel.pop();
    }
    
    Tarray ret=(Tarray) Array.newInstance
      (componentChannel.getReflector().getContentType()
      ,list.size()
      );
    
    int i=0;
    for (Titem item: list)
    { Array.set(ret,i++,item);
    }
    return ret;
  }

  @Override
  protected boolean store(
    Tarray val)
    throws AccessException
  { return false;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }

}
