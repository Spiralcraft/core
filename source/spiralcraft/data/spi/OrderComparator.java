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
package spiralcraft.data.spi;

import java.util.Comparator;

import spiralcraft.data.FieldSet;
import spiralcraft.data.Order;
import spiralcraft.data.Tuple;
import spiralcraft.data.lang.TupleReflector;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * Used to order and sort lists of Tuples
 * 
 * @author mike
 *
 */
public class OrderComparator
  implements Comparator<Tuple>
{

  private ThreadLocalChannel<Tuple> aChannel,bChannel;
  private SimpleFocus<Tuple> aFocus,bFocus;
  
  private Channel<Integer> result;
  
  public OrderComparator(Order order,FieldSet fieldSet,Focus<?> context)
    throws BindException
  {
    aChannel=new ThreadLocalChannel<Tuple>
      (TupleReflector.getInstance(fieldSet));
    aFocus=new SimpleFocus<Tuple>(context,aChannel);
    
    bChannel=new ThreadLocalChannel<Tuple>
      (TupleReflector.getInstance(fieldSet));
    bFocus=new SimpleFocus<Tuple>(context,bChannel);
    result=order.bind(aFocus,bFocus);
  }
  
  @Override
  public int compare(
    Tuple o1,
    Tuple o2)
  {
    
    aChannel.push(o1);
    bChannel.push(o2);
    try
    { return result.get();
    }
    finally
    {
      bChannel.pop();
      aChannel.pop();
    }

  }
  
  
}
