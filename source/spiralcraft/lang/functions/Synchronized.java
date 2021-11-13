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
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.util.refpool.ReferencePool;

/**
 * <p>Synchronizes the operation
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class Synchronized<T>
  implements ChannelFactory<T,T>

{

  public static final ClassLog log
    =ClassLog.getInstance(Synchronized.class);
  
  private final Binding<Object> monitorBinding;
  
  public Synchronized()
  { monitorBinding=null; 
  }
  
  public Synchronized(Binding<Object> monitorBinding)
  { this.monitorBinding=monitorBinding; 
  }
  
  @Override
  public Channel<T> bindChannel(
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    return new SynchronizedChannel
        (source
        ,monitorBinding!=null
          ?focus.bind(monitorBinding.getExpression())
          :new SimpleChannel<Object>(new Object(),true)
        );
  }
  
  public class SynchronizedChannel
    extends SourcedChannel<T,T>
  {
    private final Channel<Object> monitor;
    private final ReferencePool<Object> monitorPool=new ReferencePool<Object>();
    
    public SynchronizedChannel(Channel<T> source,Channel<Object> monitor) 
       throws BindException
    { 
      super(source);
      this.monitor=monitor;
    }
    
    private Object getMonitor()
    { return monitorPool.get(monitor.get());
    }
    
    @Override
    protected T retrieve()
    {
      synchronized (getMonitor())
      { return source.get();
      }
    }

    @Override
    protected boolean store(T val)
      throws AccessException
    { 
      synchronized (getMonitor())
      { return source.set(val);
      }
    }
  }

  
}
