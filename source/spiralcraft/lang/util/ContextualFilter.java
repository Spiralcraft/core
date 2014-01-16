//
// Copyright (c) 2014 Michael Toth
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
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * Evaluates an expression in the context of a passed parameter
 * @author mike
 *
 * @param <T>
 */
public class ContextualFilter<T>
{
  private final ThreadLocalChannel<T> subject;
  private final Channel<Boolean> result;
  
  /**
   * <p>Construct a contextual filter where the provided expression is evaluated
   *   against the input parameter as its subject within the provided context.
   * </p>
   * 
   * @param context
   * @param expression
   * @param subjectReflector
   * @throws BindException
   */
  public ContextualFilter
    (Focus<?> context
    ,Expression<Boolean> expression
    ,Reflector<T> subjectReflector
    )
    throws BindException
  { 
    subject=new ThreadLocalChannel<T>(subjectReflector);
    result=context.telescope(subject).bind(expression);
  }
  
  
  public boolean eval(final T object)
  { 
    subject.push(object);
    try
    { return Boolean.TRUE.equals(result.get());
    }
    finally
    { subject.pop();
    }
  }
}
