package spiralcraft.vfs.watcher;

import java.io.IOException;
import java.util.HashMap;

import spiralcraft.vfs.Resource;

/**
 * Caches references to objects that are derivative of VFS resources so they
 *   can be updated if the resource has changed.
 * 
 * @author mike
 *
 */
public class DerivativeCache<K,V>
{

  private HashMap<K,Entry> map
    =new HashMap<K,Entry>();
  private int pollIntervalMS=1000;
  
  
  public void put(K key,V value,Resource resource)
  { 
    synchronized (map)
    { map.put(key,new Entry(key,value,resource));
    }
  }
  
  public Entry get(K key)
    throws IOException
  { 
    Entry entry=map.get(key);
    if (entry==null)
    { return null;
    }
    else if (entry.isExpired())
    { return null;
    }
    else 
    { return entry;
    }
  }
  
  
  public class Entry
    implements WatcherHandler
  {

    private final K key;
    private final V value;
    private final ResourceWatcher watcher;
    private boolean expired;
    
    public Entry(K key,V value,Resource resource)
    {
      
      this.key=key;
      this.value=value;
      this.watcher=new ResourceWatcher(resource,pollIntervalMS,this);
    }
    
    public K getKey()
    { return key;
    }
    
    public V getValue()
    { return value;
    }

    public boolean isExpired()
      throws IOException
    { 
      if (!expired)
      { watcher.refresh();
      }
      return expired;
    }
    
    @Override
    public int handleUpdate(Resource resource)
    { 
      synchronized (map)
      { map.remove(key);
      }
      expired=true;
      
      return pollIntervalMS;
    }

  }
}
