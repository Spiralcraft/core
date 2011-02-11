//
// Copyright (c) 2011 Michael Toth
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

import spiralcraft.common.UnaryFunction;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;

/**
 * Evaluates a contextually defined function against an input parameter.
 * 
 * @author mike
 */
public abstract class ContextualFunction<I,R,X extends Exception>
  implements Contextual,UnaryFunction<I,R,X>
{
  private Channel<R> source;
  private ThreadLocalChannel<I> inputChannel;

  protected abstract Reflector<I> getInputReflector()
    throws BindException;
  
  protected abstract Channel<R> bindSource(Focus<I> inputFocus) 
    throws BindException;

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    inputChannel=new ThreadLocalChannel<I>(getInputReflector());
    source=bindSource(focusChain.chain(inputChannel));
    return focusChain;
  }
  
  /**
   * 
   */
  @Override
  public R evaluate(I input)
    throws X
  {
    inputChannel.push(input);
    try
    { return source.get();
    }
    finally
    { inputChannel.pop();
    }
  }

  
}
