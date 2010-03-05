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
package spiralcraft.beans;

import java.beans.IntrospectionException;
import java.beans.Introspector;

import java.util.HashMap;
import java.util.WeakHashMap;

import java.lang.ref.WeakReference;
import java.net.URI;

/**
 * Cache of BeanInfo (MappedBeanInfo) derived from classes with a
 *   specific set of introspector flags.
 *
 * MappedBeanInfo objects are cached, one-per-bean-class,  in a BeanInfoCache.
 *   All MappedBeanInfo objects cached in a single BeanInfoCache instance are
 *   derived usng the same set of introspector flags.
 *
 * BeanInfoCache objects themselves, one per unique introspector flag set,
 *   can be made singletons and cached in a static HashMap by using the 
 *   getInstance() method.
 *
 */
public class BeanInfoCache
{
  private static final HashMap<Integer,BeanInfoCache> _SINGLETONS
  	=new HashMap<Integer,BeanInfoCache>(); 

  private WeakHashMap<Class<?>,WeakReference<MappedBeanInfo>> _cache
  	=new WeakHashMap<Class<?>,WeakReference<MappedBeanInfo>>();
  private int _introspectorFlags;
  
  private static final WeakHashMap<URI,WeakReference<Class<?>>> uriMap
    =new WeakHashMap<URI,WeakReference<Class<?>>>();
  
  private static final HashMap<URI,Class<?>> primitiveMap
    =new HashMap<URI,Class<?>>();
  
  static
  {
    addPrimitives
      (Void.TYPE
      ,Boolean.TYPE
      ,Character.TYPE
      ,Byte.TYPE
      ,Short.TYPE
      ,Integer.TYPE
      ,Long.TYPE
      ,Float.TYPE
      ,Double.TYPE
      );
  }
  
  protected static void addPrimitives(Class<?> ... primitives)
  {
    for (Class<?> clazz:primitives)
    { primitiveMap.put(getClassURI(clazz),clazz);
    }
  }
  
  /**
   * Obtain or create the singleton instance of the BeanInfoCache which
   *   corresponds to the given introspector flags.
   */
  public static synchronized BeanInfoCache getInstance(int introspectorFlags)
  { 
    Integer flags=new Integer(introspectorFlags);

    BeanInfoCache cache=_SINGLETONS.get(flags);
    
    if (cache==null)
    { 
      cache=new BeanInfoCache(introspectorFlags);
      _SINGLETONS.put(flags,cache);
    }
    return cache;
  }
  
  /**
   * Obtain the conventional URI for the specified class, relative to
   *   the classpath. The "<code>class:/some/package/Classname</code>"
   *   convention is used. The inner class specifier "$" is replaced with 
   *   a "-".
   * 
   * @param clazz
   * @return The conventional URI for the specified class
   */
  public static final URI getClassURI(Class<?> clazz)
  { 
    String suffix="";
    while (clazz.isArray())
    { 
      suffix=suffix.concat(".array");
      clazz=clazz.getComponentType();
    }
    
    return URI.create
      ("class:/"+clazz.getName().replace('.','/').replace('$','-')+suffix);
  }
  
  /**
   * Return the Class, if any, identified by the specified URI in the 
   *   convention "<code>class:/some/package/Classname</code>".
   * 
   * @param uri
   * @return The Class identified by the URI, if the URI scheme is "class" and
   *   the Class is accessible from the context ClassLoader.
   */
  @SuppressWarnings("unchecked") 
  public static final synchronized <X> Class<X> getClassForURI(URI uri)
  { 
    if (uri==null || uri.getScheme()==null || !uri.getScheme().equals("class"))
    { return null;
    }
    
    Class result=null;
    WeakReference<Class<?>> classRef=uriMap.get(uri);
    if (classRef!=null)
    { result=classRef.get();
    }
    
    if (result==null)
    { 
      String className
        =uri.getPath().substring(1).replace('/','.').replace('-','$');

      // XXX Deal with arrays
      
      try
      {
        result 
          =Class.forName
            (className
            ,false
            ,Thread.currentThread().getContextClassLoader()
            );
        
        uriMap.put(uri,new WeakReference<Class<?>>(result));
      }
      catch (ClassNotFoundException x)
      { }
      
    }
    
    if (result==null)
    { result=primitiveMap.get(uri);
    }
    
    return result;
  }
  
  public BeanInfoCache()
  { 
  }

  public BeanInfoCache(int introspectorFlags)
  { _introspectorFlags=introspectorFlags;
  }
  

  /**
   *@return The MappedBeanInfo object for the specified Class.
   */
  public synchronized MappedBeanInfo getBeanInfo(Class<?> clazz)
    throws IntrospectionException
  {
    WeakReference<MappedBeanInfo> binfRef=_cache.get(clazz);
    MappedBeanInfo binf=null;
    if (binfRef!=null)
    { binf=binfRef.get();
    }
    
    if (binfRef==null || binf==null)
    { 
      binf=new MappedBeanInfo
        (Introspector.getBeanInfo
          (clazz,_introspectorFlags)
        ,this
        );
        
      _cache.put(clazz,new WeakReference<MappedBeanInfo>(binf));
      Introspector.flushFromCaches(clazz);
    }
    return binf;
  }

}
