package spiralcraft.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;

import java.util.HashMap;

/**
 * Non-static cache of BeanInfo derived from specific 
 *   introspector flags
 */
public class BeanInfoCache
{
  private static final HashMap _SINGLETONS=new HashMap(); 

  private HashMap _cache=new HashMap();
  private int _introspectorFlags;
  
  public static synchronized BeanInfoCache getInstance(int introspectorFlags)
  { 
    Integer flags=new Integer(introspectorFlags);

    BeanInfoCache cache=
      (BeanInfoCache) _SINGLETONS.get(flags);
    
    if (cache==null)
    { 
      cache=new BeanInfoCache(introspectorFlags);
      _SINGLETONS.put(cache,flags);
    }
    return cache;
  }
  
  public BeanInfoCache()
  {
  }

  public BeanInfoCache(int introspectorFlags)
  { _introspectorFlags=introspectorFlags;
  }

  public synchronized MappedBeanInfo getBeanInfo(Class clazz)
    throws IntrospectionException
  {
    MappedBeanInfo binf=(MappedBeanInfo) _cache.get(clazz);
    if (binf==null)
    { 
      binf=new MappedBeanInfo
        (Introspector.getBeanInfo
          (clazz,_introspectorFlags)
        );
        
      _cache.put(clazz,binf);
      Introspector.flushFromCaches(clazz);
    }
    return binf;
  }

}
