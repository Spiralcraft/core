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
package spiralcraft.util.refpool;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;

import spiralcraft.common.callable.Sink;

/**
 * <p>Pools immutable object instances to coalesce references to identical 
 *   objects into a single weak reference to reduce memory and increase 
 *   throughput.
 * </p>
 * 
 * <p>When all references to the object are released, the reference in
 *   this map will be garbage collected
 * </p>
 * 
 * @author mike
 *
 */
public class ReferencePool<T>
{
  
  private static final HashMap<Class<?>,ReferencePool<?>> typePools
    =new HashMap<Class<?>,ReferencePool<?>>();
  
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static final synchronized <T> ReferencePool<T> createPool(Class<T> clazz)
  {
    ReferencePool pool=typePools.get(clazz);
    if (pool==null)
    { 
      pool=new ReferencePool<T>();
      typePools.put(clazz,pool);
    }
    return pool;
  }
  
  /**
   * Get a static reference pool for the specified Class. Because the 
   *   reference pool maintains a reference to the Class, this should
   *   only be used for immutable classes from the bootstrap classloader,
   *   e.g. URI, BigDecimal etc.
   * 
   * @param clazz
   * @return
   */
  @SuppressWarnings("unchecked")
  public static final <T> ReferencePool<T> getInstance(Class<T> clazz)
  { 
    @SuppressWarnings("rawtypes")
    ReferencePool pool=typePools.get(clazz);
    if (pool==null)
    { pool=createPool(clazz);
    }
    return pool;
  }
  
  /**
   * Offloads lookups from the synchronized pool for use by a single thread 
   *   doing work of some finite duration. The thread cache is a strong cache,
   *   and should be discarded after use to free memory.
   * 
   * @author mike
   *
   */
  public static class ThreadCache<T>
  {
    private final ReferencePool<T> pool;
    private HashMap<T,T> cache
      =new HashMap<T,T>();
    
    public ThreadCache(ReferencePool<T> pool)
    { this.pool=pool;
    }
    
    public T get(T value)
    {
      T ret=cache.get(value);
      if (ret==null)
      { 
        ret=pool.get(value);
        cache.put(ret,ret);
      }
      return ret;
    }
    
  }   
  
  private WeakHashMap<T,WeakReference<T>> map
    =new WeakHashMap<T,WeakReference<T>>();
  
  private Sink<T> matchSink;
  private Sink<T> addSink;
  
  public void setMatchSink(Sink<T> matchSink)
  { this.matchSink=matchSink;
  }
  
  public void setAddSink(Sink<T> addSink)
  { this.addSink=addSink;
  }

  public T get(T value)
  { 
    if (value==null)
    { return null;
    }
    
    synchronized (map)
    {
      WeakReference<T> ref=map.get(value);
      if (ref!=null)
      { 
        T result=ref.get();
        if (result!=null)
        { 
          if (matchSink!=null)
          { matchSink.accept(result);
          }
          return result;
        }
      }
      map.put(value,new WeakReference<T>(value));
    }
    
    if (addSink!=null)
    { addSink.accept(value);
    }
    return value;
  }  

}
