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

import spiralcraft.common.ContextualException;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.NullChannel;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>Provides a value if the expression binds, or null if a bind error
 *   is encountered
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class IfBound<T,S>
  implements ChannelFactory<T,S>

{

  private static final ClassLog log
    =ClassLog.getInstance(IfBound.class);
  private Expression<T> expr;
  private boolean stackTrace;
  @SuppressWarnings("unchecked")
  private Reflector<T> type
    =(Reflector<T>) BeanReflector.getInstance(Void.class);
  
  public IfBound(Expression<T> expr)
  { this.expr=expr;
  }

  public IfBound(Expression<T> expr,Reflector<T> type)
  { 
    this.expr=expr;
    this.type=type;
  }

  public void setStackTrace(boolean stackTrace)
  { this.stackTrace=stackTrace;
  }
  
  @Override
  public Channel<T> bindChannel(
    Channel<S> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    focus=focus.telescope(source);
    try
    { return focus.bind(expr);
    }
    catch (ContextualException x)
    {
      if (stackTrace)
      { log.log(Level.WARNING,"Error binding `"+expr.getText()+"`",x);
      }
      else
      { log.log(Level.WARNING,"Error binding `"+expr.getText()+"`: "+x);
      }
      return new NullChannel<T>(type);
    }
  }
  
  public class IfBoundChannel
    extends SourcedChannel<S,T>
  {

    private Channel<T> channel;
    
    public IfBoundChannel(Channel<S> source,Focus<?> focus) 
       throws BindException
    { 
      super(source);
      try
      { channel=focus.bind(expr);
      }
      catch (ContextualException x)
      { 
        channel=null;
      }
    }
    
    @Override
    protected T retrieve()
    { return channel!=null?channel.get():null;
    }

    @Override
    protected boolean store(T val)
      throws AccessException
    { return channel!=null?channel.set(val):false;
    }
  }

  
}
