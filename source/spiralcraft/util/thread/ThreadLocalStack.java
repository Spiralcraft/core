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
package spiralcraft.util.thread;


/**
 * <p>A tool to manage contextual data for thread operations, for components
 *   that wish to support thread local singletons scoped to some execution
 *   subtree.
 * </p>
 *  
 * <p>Note that this usage of ThreadLocal will often be used in a non-static
 *   and re-entrant manner. To prevent memory leaks, methods that use
 *   this class to provide a "callback" should call push() before use and
 *   pop() in a finally block before returning.
 * </p>
 * 
 * <code>
 *   myThreadLocalStack.push(somevalue);
 *   try
 *   { 
 *     // Work references somevalue
 *     doWork();
 *   }
 *   finally
 *   { myThreadLocalStack.pop();
 *   }
 * </code>
 */
public class ThreadLocalStack<T>
{
  private final ThreadLocal<ThreadReference<T>> threadLocal;
  
  public ThreadLocalStack(boolean inheritable)
  { 
    if (inheritable)
    { threadLocal=new InheritableThreadLocal<ThreadReference<T>>();
    }
    else
    { threadLocal=new ThreadLocal<ThreadReference<T>>();
    }
  }

  public ThreadLocalStack()
  { threadLocal=new ThreadLocal<ThreadReference<T>>();    
  }
  
  /**
   * <p>The value that will be returned when the stack is empty
   * </p>
   * 
   * @return
   */
  public T defaultValue()
  { return null;
  }
  
  public T get()
  { 
    ThreadReference<T> r=threadLocal.get();
    if (r!=null)
    { return r.object;
    }
    else
    { return defaultValue();
    }
  }

  public void set(T val)
  { 
    ThreadReference<T> r=threadLocal.get();
    if (r!=null)
    { r.object=val;
    }
    else
    { 
      throw new IllegalStateException
        ("ThreadLocalStack not initialized ");
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
    ThreadReference<T> current=threadLocal.get();
    if (current==null)
    { throw new IllegalStateException("Stack is empty");
    }
    
    ThreadReference<T> oldref=current.prior;
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
