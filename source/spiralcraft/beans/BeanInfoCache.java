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
  private HashMap _cache=new HashMap();
  private int _introspectorFlags;

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
