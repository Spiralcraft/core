package spiralcraft.util;

import java.net.URI;
import java.util.HashMap;

import spiralcraft.util.refpool.URIPool;

public class URIResolvePool
{

  private static final URIResolvePool instance 
    =new URIResolvePool();
  
  public static URIResolvePool getInstance()
  { return instance;
  }
  
  private HashMap<String,HashMap<String,URI>> map
    =new HashMap<String,HashMap<String,URI>>();
  
  private HashMap<String,String> prefixMap
    =new HashMap<String,String>();
  
  public URI get(String prefix,String local)
  {
    URI ret;
    if (prefix.charAt(prefix.length()-1)!='/')
    { 
      String newPrefix=prefixMap.get(prefix);
      if (newPrefix==null)
      { 
        newPrefix=prefix.concat("/");
        prefixMap.put(prefix,newPrefix);
      }
      prefix=newPrefix;
    }
    
    HashMap<String,URI> ns=map.get(prefix);
    if (ns==null)
    { 
      ns=new HashMap<String,URI>();
      map.put(prefix,ns);
      ret=makeURI(prefix,local);
      ns.put(local,ret);
      
    }
    else
    {
      ret=ns.get(local);
      if (ret==null)
      { 
        ret=makeURI(prefix,local);
        ns.put(local,ret);
      }
    }
    
    return ret;
  }
  
  private URI makeURI(String prefix,String local)
  { return URIPool.create(prefix+local);
  }
}
