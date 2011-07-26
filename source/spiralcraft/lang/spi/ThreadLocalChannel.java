//
// Copyright (c) 1998,2005 Michael Toth
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


import java.net.URI;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.tree.LinkedTree;


/**
 * <p>Provides for thread-safe expression evaluation by holding a fixed value
 *   for a given Thread. Enables the sharing among multiple threads of bindings
 *   created at load-time.
 * </p>
 *  
 * <p>Note that this usage of ThreadLocal will often be used in a non-static
 *   and re-entrant manner. To prevent memory leaks, methods that use
 *   a ThreadLocalBinding to provide a "callback" value for outgoing calls 
 *   should call push() before use and pop() before returning.
 * </p>
 */
public class ThreadLocalChannel<T>
  extends AbstractChannel<T>
  implements Channel<T>
{
  private static final ClassLog log
    =ClassLog.getInstance(ThreadLocalChannel.class);
  private static final Level debugLevel
    =ClassLog.getInitialDebugLevel(ThreadLocalChannel.class,null);
  
  private final ThreadLocal<ThreadReference<T>> threadLocal;
  private final boolean inheritable;
  private final Channel<T> sourceChannel;
  private final Channel<?> origin;
  private final Exception initTrace
    =new Exception("ThreadLocal allocation stack:");

  class InheritableThreadLocalImpl
    extends InheritableThreadLocal<ThreadReference<T>>
  {
    @Override
    protected ThreadReference<T> childValue(ThreadReference<T> parentValue)
    { 
      if (debugLevel.canLog(Level.DEBUG))
      {
        log.debug
          ("Inheriting "
          +getReflector().getTypeURI()+" : "
          +(parentValue!=null
            ?parentValue.object.toString()
            :"(uninitialized)"
           )
          );
      }
      return parentValue;
    }
    
  }
  
  public ThreadLocalChannel(final Reflector<T> reflector,boolean inheritable)
  { 
    super(reflector);
    this.inheritable=inheritable;
    if (inheritable)
    { threadLocal=new InheritableThreadLocalImpl();
    }
    else
    { threadLocal=new ThreadLocal<ThreadReference<T>>();
    }
    sourceChannel=null;
    origin=null;
  }

  public ThreadLocalChannel(final Reflector<T> reflector,boolean inheritable,Channel<?> origin)
  { 
    super(reflector);
    this.inheritable=inheritable;
    if (inheritable)
    { threadLocal=new InheritableThreadLocalImpl();
    }
    else
    { threadLocal=new ThreadLocal<ThreadReference<T>>();
    }
    sourceChannel=null;
    this.origin=origin;
  }

  public ThreadLocalChannel(final Channel<T> sourceChannel,boolean inheritable)
  { 
    super(sourceChannel.getReflector());
    this.inheritable=inheritable;
    if (inheritable)
    { threadLocal=new InheritableThreadLocalImpl();
    }
    else
    { threadLocal=new ThreadLocal<ThreadReference<T>>();
    }
    this.sourceChannel=sourceChannel;
    this.origin=sourceChannel;
    this.context=sourceChannel.getContext();
  }

  public ThreadLocalChannel(Reflector<T> reflector)
  { 
    super(reflector);
    threadLocal=new ThreadLocal<ThreadReference<T>>();    
    inheritable=false;
    this.sourceChannel=null;
    this.origin=null;

  }
  
  public boolean isInheritable()
  { return inheritable;
  }
  
  @Override
  public boolean isWritable()
  { return true;
  }
  
  @Override
  public T retrieve()
  { 
    ThreadReference<T> r=threadLocal.get();
    if (r!=null)
    { return r.object;
    }
    else if (sourceChannel!=null)
    { return sourceChannel.get();
    }
    else
    {
      AccessException x=new AccessException
        ("ThreadLocal not initialized for "+getReflector().getTypeURI());
      log.log(Level.WARNING,x.getMessage(),x);
      log.log(Level.WARNING,"ThreadLocal initializer trace: ",initTrace);
      throw x;
    }
  }

  @Override
  public boolean store(T val)
  { 
    ThreadReference<T> r=threadLocal.get();
    if (r!=null)
    { 
      r.object=val;
      return true;
    }
    else if (sourceChannel!=null)
    { return sourceChannel.set(val);
    }
    else
    { 
      AccessException x=new AccessException
        ("ThreadLocal not initialized for "+getReflector().getTypeURI());
      log.log(Level.WARNING,"ThreadLocal initializer trace: ",initTrace);
      log.log(Level.WARNING,x.getMessage(),x);
      throw x;
    }

  }
  
  /**
   * Call when 
   */
  public void remove()
  { threadLocal.remove();
  }

  
  /**
   * Use the current value of the source channel (or null) as the local value
   *    for use by all outgoing method calls.
   */
  public void push()
  {
    if (sourceChannel!=null)
    { push(sourceChannel.get());
    }
    else
    { push(null);
    }
  }
  
  /**
   * Provide a new local value for use by all outgoing method calls.
   */
  public void push(T val)
  { 
    ThreadReference<T> oldref=threadLocal.get();
    ThreadReference<T> newref=new ThreadReference<T>();
    newref.object=val;
    newref.prior=oldref;
    threadLocal.set(newref);
  }
  
  /**
   * Restore the value of this threadLocal before push() was called.
   */
  public void pop()
  { 
    ThreadReference<T> oldref=threadLocal.get().prior;
    if (oldref==null)
    { threadLocal.remove();
    }
    else
    { threadLocal.set(oldref);
    }
  }
  
  /**
   * Resolve contextual metadata for this Channel from an appropriate provider
   *   in the Channel graph
   * 
   * @param <X>
   * @param focus
   * @param metadataTypeURI
   * @return
   * @throws BindException 
   */
  @Override
  public <X> Channel<X> resolveMeta(Focus<?> focus,URI metadataTypeURI) 
    throws BindException
  { 
    Channel<X> meta=super.resolveMeta(focus,metadataTypeURI);
    if (meta==null && origin!=null)
    { meta=origin.resolveMeta(focus,metadataTypeURI);
    }
    return meta;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public LinkedTree<Channel<?>> trace(Class<Channel<?>> stop)
  { 
    if (origin!=null)
    { return new LinkedTree<Channel<?>>(this,origin.trace(stop));
    }
    else
    { return new LinkedTree<Channel<?>>(this);
    }
  }  
  
  public Exception getInitTrace()
  { return initTrace;
  }
  
  class ThreadReference<X>
  {
    public ThreadReference<X> prior;
    public X object;
  }

}
