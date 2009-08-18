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

import java.util.Comparator;

import spiralcraft.data.lang.TypeReflector;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.util.lang.ClassUtil;

/**
 * A group of OrderElements describes how to sort a set of objects.
 * 
 * @author mike
 *
 */
public class OrderElement<T>
{
  private static final ClassLog log
    =ClassLog.getInstance(OrderElement.class);
    
  private Expression<T> expression;
  private int weight=1;
  private Comparator<T> specifiedComparator;
  private boolean nullLast;
  private boolean debug;
  
  /**
   * Construct an OrderElement from a String, which contains an optional
   *   indicator for direction as the first character, and an expression
   *   as the remainder. The default Collator for the Locale will be used.
   * 
   * @param ordering
   */
  public OrderElement(String ordering)
    throws ParseException
  { setOrdering(ordering);
  }

  public OrderElement()
  { }
    
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * Specify the ordering description, which contains an optional
   *   indicator for direction as the first character, and an expression
   *   as the remainder. 
   * @param val
   * @throws ParseException
   */
  public void setOrdering(String val)
    throws ParseException
  {
    if (val!=null && val.length()>0)
    {
      if (val.charAt(0)=='+')
      { 
        weight=1;
        expression=Expression.parse(val.substring(1));
      }
      else if (val.charAt(0)=='-')
      { 
        weight=-1;
        expression=Expression.parse(val.substring(1));
      }
      else
      { expression=Expression.parse(val);
      }
    }
    if (expression==null)
    { throw new IllegalArgumentException("Must contain an expression: "+val);
    }
  }
  
  public void setExpression(Expression<T> expression)
  { this.expression=expression;
  }
  
  public Expression<T> getExpression()
  { return expression;
  }
  
  public boolean getReverse()
  { return weight==-1;
  }
    
  public void setReverse(boolean reverse)
  { 
    if (reverse)
    { this.weight=-1;
    }
    else
    { this.weight=1;
    }
  }
  
  public Comparator<T> getComparator()
  { return specifiedComparator;
  }
  
  public void setComparator(Comparator<T> comparator)
  { this.specifiedComparator=comparator;
  }
  
  public void setNullLast(boolean val)
  { this.nullLast=val;
  }
  
  public boolean isNullLast()
  { return nullLast;
  }
  
  public Channel<Integer> bind(Focus<?> focusA,Focus<?> focusB)
    throws BindException
  {
    return new OrderElementChannel
      (focusA.bind(expression)
      ,focusB.bind(expression)
      ); 
  }
  
  

  
  public class OrderElementChannel
    extends AbstractChannel<Integer>
  {
    private final Channel<T> chA,chB;
    private Comparator<T> comparator=OrderElement.this.specifiedComparator;
    
    @SuppressWarnings("unchecked")
    public OrderElementChannel(Channel<T> chA,Channel<T> chB)
      throws BindException
    { 
      super(BeanReflector.<Integer>getInstance(Integer.class));
      this.chA=chA;
      this.chB=chB;
      if (comparator==null)
      {
        if (chA.getReflector() instanceof TypeReflector)
        {
          Type type=((TypeReflector) chA.getReflector()).getType();
          if (type!=null)
          { comparator=type.getComparator();
          }
        }
      }
      
      if (comparator==null)
      {
        if (Comparable.class.isAssignableFrom
            (ClassUtil.boxedEquivalent(chA.getContentType())))
        { comparator=new DefaultComparator();
        }
      }
      
      if (comparator==null)
      { 
        throw new BindException
          ("Unable to determine suitable comparator for "+chA.getContentType());
        
      }
      if (OrderElement.this.debug)
      { log.fine("Comparator is "+comparator);
      }
    }

    @Override
    protected Integer retrieve()
    {

      T o1=chA.get();
      T o2=chB.get();
      if (o1==null)
      { 
        if (o2!=null)
        { return weight*(nullLast?1:-1);
        }
        else
        { return 0;
        }
      }
      else
      {
        if (o2==null)
        { return weight*(nullLast?-1:1);
        }
        else
        {
          return weight*(comparator.compare(o1,o2));
        }
      }
      
    }

    @Override
    protected boolean store(
      Integer val)
      throws AccessException
    {
      // TODO Auto-generated method stub
      return false;
    }
  }
  
 
}

class DefaultComparator<X extends Comparable<X>>
  implements Comparator<X>
{
    public int compare(
      X o1,
      X o2)
    { return o1.compareTo(o2);
    }
}

