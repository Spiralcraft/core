package spiralcraft.sax;

import java.net.URI;
import java.util.HashMap;

import spiralcraft.common.NamespaceResolver;

/**
 * Resolves namespace prefixes in a memory efficient manner
 * 
 * @author mike
 */
public class PrefixResolver
  implements NamespaceResolver
{
  private final NamespaceResolver parent;
  private final HashMap<String,URI> map
    =new HashMap<String,URI>();
  
  public PrefixResolver()
  { this.parent=null;
  }
  
  public PrefixResolver(NamespaceResolver parent)
  { this.parent=parent;
  }
  
  public URI resolvePrefix(String prefix)
  {
    URI uri=map.get(prefix);
    if (uri!=null)
    { return uri;
    } 
    else if (parent!=null)
    { return parent.resolvePrefix(prefix);
    }
    return null;
  }
  
  public URI getDefaultURI()
  { 
    URI ret=map.get("default");
    if (ret!=null)
    { return ret;
    }
    else if (parent!=null)
    { return parent.getDefaultURI();
    }
    else
    { return null;
    }
  }
  
  public void mapPrefix(String prefix,URI uri)
  { map.put(prefix,uri);
  }
}
