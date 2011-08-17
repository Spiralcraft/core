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
package spiralcraft.lang.kit;

import spiralcraft.common.UnaryFunction;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>An Expression that will be evaluated in the scope of an input parameter.
 * </p>
 * 
 * @author mike
 *
 */
public class Callable<I,O>
  implements UnaryFunction<I,O,RuntimeException>
{
  private final ThreadLocalChannel<I> inputChannel;
  private Channel<O> channel;
  
  public Callable(Focus<?> focus,Reflector<I> inputType,Expression<O> expr)
    throws BindException
  { this(focus,inputType,new Binding<O>(expr));
  }

  public Callable(Focus<?> focus,Reflector<I> inputType,Binding<O> binding)
    throws BindException
  { 
    inputChannel=new ThreadLocalChannel<I>(inputType,true,binding);
    binding.bind(focus.chain(inputChannel));
    channel=binding;
  }

  @Override
  public O evaluate(I input)
  { 
    inputChannel.push(input);
    try
    { return channel.get();
    }
    finally
    { inputChannel.pop();
    }
  }
}
