//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.lang.functions;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.Callable;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * Repeats evaluation of its input until a condition is met
 * 
 * @author mike
 *
 * @param <T>
 */
public class Repeat<T>
  implements ChannelFactory<T,T>

{

  private Expression<Boolean> condition;
  
  public Repeat(Expression<Boolean> condition)
  { this.condition=condition;
  }
  
  @Override
  public Channel<T> bindChannel(
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    return new RepeatChannel(source,focus);
  }
  
  public class RepeatChannel
    extends SourcedChannel<T,T>
  {
    private final Callable<T,Boolean> callable;
    
    public RepeatChannel(Channel<T> source,Focus<?> focus) 
       throws BindException
    { 
      super(source);
      callable
        =new Callable<T,Boolean>
          (focus
          ,source.getReflector()
          ,new Binding<Boolean>(condition)
          );
    }
    
    @Override
    public boolean isWritable()
    { return false;
    }
    
    @Override
    protected T retrieve()
    { 
      T val;
      do
      { val=source.get();
      }
      while (!Boolean.TRUE.equals(callable.evaluate(val)));
      return val;
    }

    @Override
    protected boolean store(
      T val)
      throws AccessException
    { return false;
    }
  }

  
}
