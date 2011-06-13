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
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>Prevents an exception from terminating a computation
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class Try<T>
  implements ChannelFactory<T,T>

{

  public static final ClassLog log
    =ClassLog.getInstance(Try.class);
  private boolean stackTrace=true;
  private String message="Evaluation error";
  
  public Try()
  { 
  }
  
  public void setStackTrace(boolean stackTrace)
  { this.stackTrace=stackTrace;
  }
  
  public void setMessage(String message)
  { this.message=message;
  }
  
  @Override
  public Channel<T> bindChannel(
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    return new TryChannel(source);
  }
  
  public class TryChannel
    extends SourcedChannel<T,T>
  {

    public TryChannel(Channel<T> source) 
       throws BindException
    { super(source);
    }
    
    @Override
    protected T retrieve()
    {
      try
      { return source.get();
      }
      catch (Exception x)
      { 
        if (stackTrace)
        { log.log(Level.WARNING,message,x);
        }
        else
        { log.log(Level.WARNING,message+": (get) :"+x);
        }
        return null;
      }
    }

    @Override
    protected boolean store(T val)
      throws AccessException
    { 
      try
      { return source.set(val);
      }
      catch (Exception x)
      { 
        if (stackTrace)
        { log.log(Level.WARNING,message,x);
        }
        else
        { log.log(Level.WARNING,message+": (set) :"+x);  
        }
        return false;
      }
    }
  }

  
}
