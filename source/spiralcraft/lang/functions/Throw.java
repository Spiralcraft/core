//
// Copyright (c) 2021 Michael Toth
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
import spiralcraft.lang.spi.SourcedChannel;

/**
 * A ChannelFactory which throws an exception
 * 
 * @author mike
 *
 */
public class Throw
  implements ChannelFactory<Void,Void>
{

  private Expression<Throwable> thrown;
  
  public Throw()
  { 
  }
  
  public Throw(Expression<Throwable> thrown)
  { this.thrown=thrown;
  }
  
  @Override
  public Channel<Void> bindChannel(
    Channel<Void> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    return new ThrowChannel
      (focus
      ,source
      );
  }
  
  public class ThrowChannel
    extends SourcedChannel<Void,Void>
  {

    private Channel<Throwable> thrownBinding;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ThrowChannel
      (Focus<?> focus
      ,Channel<Void> source
      )
      throws BindException
    { 
      super(source.getReflector(),source);
      if (thrown!=null)
      { thrownBinding=focus.chain(source).bind(thrown);
      }
    }
    
    
    @Override
    protected Void retrieve()
    { 
      if (thrownBinding!=null)
      { 
        Throwable throwable=thrownBinding.get();
        if (throwable instanceof RuntimeException)
        { throw (RuntimeException) throwable;
        }
        else
        { throw new AccessException(throwable);
        }
      }
      else
      { throw new AccessException("");
      }
    }
  
    
    @Override
    protected boolean store(Void val)
      throws AccessException
    { return false;
    } 
  }

}
