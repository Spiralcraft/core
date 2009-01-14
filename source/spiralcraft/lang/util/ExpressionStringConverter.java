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
package spiralcraft.lang.util;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.util.string.StringConverter;

/**
 * <p>A StringConverter which returns the result of an Expression evaluated
 *   on the input String.
 * </p>
 * 
 * <p>The "." prefix is used in the Expression to denote the String under
 *   consideration (eg. " <code>.substring(1).trim()</code> ").
 * </p>
 * 

 * @author mike
 *
 */
public final class ExpressionStringConverter<T>
  extends StringConverter<T>
{
  
  
  private ThreadLocalChannel<String> stringLocal;
  private Channel<T> result;

  public ExpressionStringConverter
    (Focus<?> focus
    ,Expression<T> expression
    ,Reflector<T> formalType
    )
    throws BindException
  { 
    stringLocal
      =new ThreadLocalChannel<String>
        (BeanReflector.<String>getInstance(String.class));
    result=focus.telescope(stringLocal).bind(expression);
    if (formalType!=null && !formalType.isAssignableFrom(result.getReflector()))
    { 
      throw new BindException
        (formalType.getTypeURI()
        +" cannot be assigned from "
        +result.getReflector().getTypeURI()
        );
    }
  }
  
  
  public Channel<?> getResultChannel()
  { return result;
  }
  
  @Override
  public String toString(T val)
  { 
    stringLocal.push(null);
    try
    {
      result.set(val);
      return stringLocal.get();
    }
    finally
    { stringLocal.pop();
    }

  }
    

  @Override
  public T fromString(String val)
  { 
    stringLocal.push(val);
    try
    { return result.get();
    }
    finally
    { stringLocal.pop();
    }
      

  }
}