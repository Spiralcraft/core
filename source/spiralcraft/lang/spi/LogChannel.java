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
package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;

/**
 * <p>Logs a message when a value is retrieved or stored
 * </p>
 * 
 * @author mike
 *
 */
public class LogChannel<T>
  extends SourcedChannel<T,T>
  implements Channel<T>
{
  private static final ClassLog log=ClassLog.getInstance(LogChannel.class);
  private static volatile int nextId=1;
  
  private final Channel<?> message;
  private final ThreadLocalChannel<T> localSource;
  private final int id=nextId++;
  
  public LogChannel(Channel<T> source,Focus<?> focus,Expression<?> message)
    throws BindException
  { 
    super(source.getReflector(),source);
    this.localSource=new ThreadLocalChannel<T>(source.getReflector(),true,source);
    this.message=focus.telescope(localSource).bind(message);
    log.debug
      ("#"+id
        +": channeling "+source.toString()
      );
  }
  
  @Override
  protected T retrieve()
  { 
    T value=source.get();
    localSource.push(value);
    try
    { log.debug("#"+id+": get(): "+message.get());
    }
    finally
    { localSource.pop();
    }
    return value;
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { 
    boolean result=source.set(val);
    localSource.push(val);
    try
    { log.debug("#"+id+": set()=="+result+": "+message.get());
    }
    finally
    { localSource.pop();
    }
    return result;
  }

  @Override
  public boolean isWritable()
  { return source.isWritable();
  }
  
  @Override
  public boolean isConstant()
  { return source.isConstant();
  }
  
}
