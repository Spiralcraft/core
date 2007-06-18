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
package spiralcraft.lang.optics;


import spiralcraft.lang.optics.Prism;


/**
 * <P>Provides for thread-safe expression evaluation by holding a fixed value
 *   for a given Thread. Enables the sharing among multiple threads of bindings
 *   created at load-time.
 *   
 * <P>Note that this usage of ThreadLocal will often be used in a non-static
 *   and re-entrant manner. To prevent memory leaks, methods that with to use
 *   a ThreadLocalBinding to provide values for outgoing calls should call
 *   push() before use and pop() before returning.
 */
public class ThreadLocalBinding<T>
  extends AbstractBinding<T>
{
  private final ThreadLocal<Reference<T>> threadLocal
    =new ThreadLocal<Reference<T>>();
  
  public ThreadLocalBinding(Prism<T> prism)
  { super(prism);
  }
  
 
  @Override
  public T retrieve()
  { return threadLocal.get().object;
  }

  @Override
  public boolean store(T val)
  { 
    threadLocal.get().object=val;
    return true;
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
    Reference<T> oldref=threadLocal.get();
    Reference<T> newref=new Reference<T>();
    newref.object=val;
    newref.prior=oldref;
    threadLocal.set(newref);
  }
  
  /**
   * Restore the value of this threadLocal before push() was called.
   */
  public void pop()
  { 
    Reference<T> oldref=threadLocal.get().prior;
    if (oldref==null)
    { threadLocal.remove();
    }
    else
    { threadLocal.set(oldref);
    }
  }
  
  class Reference<X>
  {
    public Reference<X> prior;
    public X object;
  }

}
