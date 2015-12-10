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

//import spiralcraft.log.ClassLog;
//import spiralcraft.log.Level;

import spiralcraft.common.DisposableReference;

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
//  private static final ClassLog log
//    =ClassLog.getInstance(ClassLoaderLocal.class);
  
  // TODO: Make a reference to T so we don't have a strong ref to classloader
  private final WeakHashMap<ClassLoader,DisposableReference<T>> map=new WeakHashMap<>();

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
    { map.put(loader,new DisposableReference<T>(instance));
    }
  }
  
  /**
   * Assign the specified instance to the specified ClassLoader
   */
  public void setInstance(ClassLoader loader,T instance)
  { 
//    log.log(Level.FINE,"Created ref "+instance+" for "+loader,new Exception("trace"));
    if (map.get(loader)==null || map.get(loader).get()==null)
    { map.put(loader,new DisposableReference<T>(instance));
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
    DisposableReference<T> ref=map.get(loader);
    if (ref!=null)
    { 
      if (ref.get()==null)
      { // log.warning("Ref disappeared for "+loader);
      }
      return ref.get();
    }
    return null;
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
  
  /**
   * 
   * @return The instance for the nearest ancestor classloader that has a
   *   non-null instance
   */
  public T getNearestInstance()
  { 
    ClassLoader loader=Thread.currentThread().getContextClassLoader();
    boolean reachedSystem=false;
    while (loader!=null)
    { 
      if (loader==ClassLoader.getSystemClassLoader())
      { reachedSystem=true;
      }
      
      T instance=getInstance(loader);
      if (instance!=null)
      { return instance;
      }
      loader=loader.getParent();
      
      if (loader==null && !reachedSystem)
      { loader=ClassLoader.getSystemClassLoader();
      }
    }
    return null;
    
  }
  
  
}