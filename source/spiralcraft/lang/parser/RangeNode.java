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
package spiralcraft.lang.parser;


import spiralcraft.lang.AccessException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Range;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;



/**
 * Represents a range of numbers
 * 
 * @author mike
 *
 * @param <T> The base (content) type
 * @param <C> The collection type
 * @param <I> The index or selector type
 */
public class RangeNode<T>
  extends Node
{

  private final Node start;
  private final Node end;
  private boolean inclusive;

  public RangeNode(Node start,Node end,boolean inclusive)
  { 
    this.start=start;
    this.end=end;
    this.inclusive=inclusive;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {start,end};
  }
    
  @Override
  public Node copy(Object visitor)
  { 
    RangeNode<T> copy
      =new RangeNode<T>
      (start!=null?start.copy(visitor):null
      ,end!=null?end.copy(visitor):null
      ,inclusive
      );
    if (copy.start==start && copy.end==end)
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { 
    return start.reconstruct()+" "
            +(inclusive?"..":".!")
            +" "+
            (end!=null
               ?end.reconstruct()
               :""
             );
            
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Channel<?> bind(final Focus<?> focus)
    throws BindException
  {
    
    final Channel<Number> endChannel=end!=null?focus.bind(new Expression(end)):null;
    final Channel<Number> startChannel=start!=null?focus.bind(new Expression(start)):null;
    
    return new AbstractChannel(BeanReflector.getInstance(Range.class))
    {

      { this.context=focus;
      }
      
      @Override
      protected Object retrieve()
      { 
        return new Range
          (startChannel!=null?startChannel.get():null
          ,endChannel!=null?endChannel.get():null
          ,inclusive
          );
      }

      @Override
      protected boolean store(
        Object val)
        throws AccessException
      {
        // TODO Auto-generated method stub
        return false;
      }
    };

  }
  
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Range");
    prefix=prefix+"  ";
    if (start!=null)
    { start.dumpTree(out,prefix);
    }
    out.append(prefix).append(inclusive?"..":".!");
    if (end!=null)
    { start.dumpTree(out,prefix);
    }

  }
  
}