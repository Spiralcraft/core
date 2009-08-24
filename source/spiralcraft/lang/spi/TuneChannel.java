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
 * <p>Computes call time for calls to retrieve and store in order to
 *   measure latency at various points in a Channel graph.
 * </p>
 * 
 * @author mike
 *
 */
public class TuneChannel<T>
  extends AbstractChannel<T>
  implements Channel<T>
{
  private static final ClassLog log=ClassLog.getInstance(TuneChannel.class);
  private static volatile int nextId=1;
  
  private final Channel<T> source;
  private final Channel<?> message;
  private final ThreadLocalChannel<T> localSource;
  private final int id=nextId++;
  
  public TuneChannel(Channel<T> source,Focus<?> focus,Expression<?> message)
    throws BindException
  { 
    super(source.getReflector());
    this.source=source;
    this.localSource=new ThreadLocalChannel<T>(source.getReflector());
    this.message=focus.telescope(localSource).bind(message);
    log.debug("#"+id+": created");
  }
  
  @Override
  protected T retrieve()
  { 
    log.debug("#"+id+": get() called");
    
    long startTime=System.nanoTime();
    T value=source.get();
    long elapsedTime=System.nanoTime()-startTime;
    
    localSource.push(value);
    try
    { 
      log.debug
        ("#"+id+": get() (took "+(0.000001*elapsedTime)+" ms): "
          +message.get()
        );
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
    
    localSource.push(val);

    try
    { log.debug("#"+id+": set() called: "+message.get());
    }
    finally
    { localSource.pop();
    }
    
    long startTime=System.nanoTime();
    boolean result=source.set(val);
    long elapsedTime=System.nanoTime()-startTime;
    
    log.debug
      ("#"+id+": set()=="+result+" :(took "+(0.000001*elapsedTime)+" ms)");
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
