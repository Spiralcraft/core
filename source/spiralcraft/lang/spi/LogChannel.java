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

import spiralcraft.common.declare.Declarable;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.functions.ToString;
import spiralcraft.lang.util.LangUtil;
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
  
  private final Channel<String> message;
  private final ThreadLocalChannel<T> localSource;
  private final int id=nextId++;
  
  @SuppressWarnings("unchecked")
  public LogChannel(Channel<T> source,Focus<?> focus,Expression<?> message)
    throws BindException
  { 
    super(source.getReflector(),source);
    this.localSource=new ThreadLocalChannel<T>(source.getReflector(),true,source);
    
    Channel<Object> messageObject
      =focus.telescope(localSource).bind((Expression<Object>) message);
    
    this.message
      =new ToString<Object>
        (messageObject.getReflector().getStringConverter())
          .bindChannel
            (messageObject
            ,focus
            ,null
            );
    
    Declarable declarable = LangUtil.findInstance(Declarable.class,focus);
    log.debug
      ("#"+id
        +": channeling "+source.toString()
        +(declarable!=null?(": "+declarable.getDeclarationInfo()):"")
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
