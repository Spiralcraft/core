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
package spiralcraft.lang;

import spiralcraft.lang.spi.ProxyChannel;

/**
 * <p>A bindable Expression that contextualizes the behavior of a component.
 * </p>
 * 
 * <p>Usually supplied to the component via a bean property as a result
 *   of the Expression text being supplied in a code artifact or
 *   configuration mechanism
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class Binding<T>
  extends ProxyChannel<T>
  implements Channel<T>
{

  private final Expression<T> expression;
  
  public Binding(Expression<T> expression)
  { this.expression=expression;
  }
  
  public Binding(String expression)
    throws ParseException
  { this.expression=Expression.parse(expression);
  }
  
  /**
   * Create a pre-bound binding to
   * 
   * @param channel
   */
  public Binding(Channel<T> channel)
  { 
    this.channel=channel;
    this.expression=null;
  }
    
  public Expression<T> getExpression()
  { return expression;
  }
  
  public String getText()
  { return expression!=null?expression.getText():null;
  }
  
  public void bind(Focus<?> focus)
    throws BindException
  { 
    if (expression==null && channel!=null)
    { 
      // Pre-bound
      return;
    }
    
    if (channel==null)
    { channel=focus.bind(expression);
    }
  }
  
  public void reset()
  { 
    if (expression!=null)
    { 
      // Only non pre-bound expression can be reset
      channel=null;
    }
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+expression+" => "+channel;
  }
}
