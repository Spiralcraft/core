package spiralcraft.lang.optics;

import java.util.HashMap;

public class WeakBindingCache
{
  private HashMap _map=new HashMap();
  
  public synchronized Binding get(Object key)
  { return (Binding) _map.get(key);
  }
  
  public synchronized void put(Object key,Binding value)
  { _map.put(key,value);
  }
}

