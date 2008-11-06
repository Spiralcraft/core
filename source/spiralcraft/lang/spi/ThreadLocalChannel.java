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


import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;


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
  private final ThreadLocal<ThreadReference<T>> threadLocal;
  
  public ThreadLocalChannel(Reflector<T> reflector,boolean inheritable)
  { 
    super(reflector);
    if (inheritable)
    { threadLocal=new InheritableThreadLocal<ThreadReference<T>>();
    }
    else
    { threadLocal=new ThreadLocal<ThreadReference<T>>();
    }
  }

  public ThreadLocalChannel(Reflector<T> reflector)
  { 
    super(reflector);
    threadLocal=new ThreadLocal<ThreadReference<T>>();    
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
    else
    { 
      throw new AccessException
        ("ThreadLocal not initialized for "+getReflector().getTypeURI());
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
    else
    { 
      throw new AccessException
        ("ThreadLocal not initialized for "+getReflector().getTypeURI());
    }

  }
  
  /**
   * Call when 
   */
  public void remove()
  { threadLocal.remove();
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
  
  class ThreadReference<X>
  {
    public ThreadReference<X> prior;
    public X object;
  }

}
