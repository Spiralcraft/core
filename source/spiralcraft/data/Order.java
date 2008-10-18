//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data;


import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.log.ClassLogger;
import spiralcraft.util.ArrayUtil;

/**
 * A sorting function for a set of Typed data. Uses OrderElements to define
 *   the individual terms of the sort.
 * 
 * @author mike
 */
public class Order
{

  private OrderElement<?>[] elements;
  private final ClassLogger log=ClassLogger.getInstance(Order.class);
  private boolean debug;
  
  public void setElements(OrderElement<?>[] elements)
  { this.elements=elements;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * Returns a binding to the equivalent of the result of the 
   *   Comparator.compare(a,b) function determined by the aggregate of all the
   *   OrderElements. Earlier elements take preference.
   * 
   * @param focusA
   * @param focusB
   * @return
   */
  public Channel<Integer> bind(Focus<Tuple> focusA,Focus<Tuple> focusB)
    throws BindException
  {
    return new OrderChannel
      (focusA,focusB);
  }

  public class OrderChannel
    extends AbstractChannel<Integer>
  {
     
    public Channel<Integer>[] elementChannels;

    @SuppressWarnings("unchecked")
    public OrderChannel(Focus<Tuple> focusA,Focus<Tuple> focusB)
      throws BindException
    { 
      super(BeanReflector.<Integer>getInstance(Integer.class));
      if (debug || Order.this.debug)
      { log.fine("OrderChannel ["+ArrayUtil.format(elements,"],[","")+"]");
      }
      elementChannels=new Channel[elements.length];
      for (int i=0;i<elements.length;i++)
      { elementChannels[i]=elements[i].bind(focusA,focusB);
      }
    }

    @Override
    protected Integer retrieve()
    {
      for (Channel<Integer> channel: elementChannels)
      { 
        int ret;
        if ((ret=channel.get())!=0)
        { return ret;
        }
      }
      return 0;
    }

    @Override
    protected boolean store(
      Integer val)
      throws AccessException
    { return false;
    }
    

  }
  
}
