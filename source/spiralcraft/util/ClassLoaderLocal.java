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

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * <p>A tool which permits classes to associate an instance with a ClassLoader.
 * </p>
 *
 * <p>This class is not synchronized. 
 * </p>
 *
 * <p>An instance of this Class is intended to be used as a static member of
 *   class <T> where a synchronized static method in <T> interacts with the
 *   methods of this class to return a singleton associated with the current
 *   or parent ClassLoader.
 * </p>
 */
public class ClassLoaderLocal<T>
{
  // TODO: Make a reference to T so we don't have a strong ref to classloader
  private final WeakHashMap<ClassLoader,T> map=new WeakHashMap<ClassLoader,T>();

  /**
   * Return the instance associated with this thread's contextClassLoader
   */
  public T getContextInstance()
  {
    ClassLoader loader=Thread.currentThread().getContextClassLoader();
    if (loader!=null)
    { return getInstance(loader);
    }
    else
    { return getInstance(ClassLoader.getSystemClassLoader());
    }
    
  }
  

  
  /**
   * Return the instance associated with this thread's contextClassLoader's
   *   parent ClassLoader
   */
  public T getParentContextInstance()
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
  public void setContextInstance(T instance)
  {
    ClassLoader loader=Thread.currentThread().getContextClassLoader();
    if (loader!=null)
    { map.put(loader,instance);
    }
  }
  
  /**
   * Assign the specified instance to the specified ClassLoader
   */
  public void setInstance(ClassLoader loader,T instance)
  { 
    if (map.get(loader)==null)
    { map.put(loader,instance);
    }
    else
    { 
      throw new IllegalStateException
        ("Instance already mapped to ClassLoader "+instance);
    }

  }

  public T getInstance(ClassLoader loader)
  { 
    // Tickle map to flush GC'd keys
    map.isEmpty(); 

    return map.get(loader);
  }
  
  /**
   * 
   * @return The instances for the thread context Classloader and all 
   *   parent Classloaders, starting with the thread context Classloader.
   */
  public List<T> getAllContextInstances()
  {
    LinkedList<T> list=new LinkedList<T>();
    ClassLoader loader=Thread.currentThread().getContextClassLoader();
    boolean reachedSystem=false;
    while (loader!=null)
    { 
      if (loader==ClassLoader.getSystemClassLoader())
      { reachedSystem=true;
      }
      
      T instance=getInstance(loader);
      if (instance!=null)
      { list.add(instance);
      }
      loader=loader.getParent();
      
      if (loader==null && !reachedSystem)
      { loader=ClassLoader.getSystemClassLoader();
      }
    }
    
    return list;
  }
  
}