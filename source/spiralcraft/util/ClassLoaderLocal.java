//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.util;

import java.util.WeakHashMap;

/**
 * A tool which permits classes to associate an instance with a ClassLoader.
 *
 * This class is not synchronized. 
 *
 * An instance of this Class is intended to be used as a static member of
 *   class <T> where a synchronized static method in <T> interacts with the
 *   methods of this class to return a singleton associated with the current
 *   or parent ClassLoader.
 */
public class ClassLoaderLocal<T>
{
  private final WeakHashMap<ClassLoader,T> map=new WeakHashMap<ClassLoader,T>();

  /**
   * Return the instance associated with this thread's contextClassLoader
   */
  public T getInstance()
  {
    ClassLoader loader=Thread.currentThread().getContextClassLoader();
    if (loader!=null)
    { return getInstance(loader);
    }
    else
    { return getInstance(ClassLoader.getSystemClassLoader());
    }
    
  };
  
  
  /**
   * Return the instance associated with this thread's contextClassLoader's
   *   parent ClassLoader
   */
  public T getParentInstance()
  {
    ClassLoader loader=Thread.currentThread().getContextClassLoader();
    if (loader==null)
    { 
      // System ClassLoader has no parent
      return null;
    }

    loader=loader.getParent();
    if (loader!=null)
    { return getInstance(loader);
    }
    else
    { return getInstance(ClassLoader.getSystemClassLoader());
    }
  }
  
  /**
   * Assign the specified instance to this thread's contextClassLoader
   */
  public void setInstance(T instance)
  {
    ClassLoader loader=Thread.currentThread().getContextClassLoader();
    if (loader!=null)
    { map.put(loader,instance);
    }
  }
  
  private T getInstance(ClassLoader loader)
  { 
    // Tickle map to flush GC'd keys
    map.isEmpty(); 

    return map.get(loader);
  }
}