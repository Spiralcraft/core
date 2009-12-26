package spiralcraft.sax;

import java.net.URI;
import java.util.HashMap;

import spiralcraft.common.namespace.PrefixResolver;

/**
 * Resolves namespace prefixes in a memory efficient manner
 * 
 * @author mike
 */
public class SaxPrefixResolver
  implements PrefixResolver
{
  private final PrefixResolver parent;
  private final HashMap<String,URI> map
    =new HashMap<String,URI>();
  
  public SaxPrefixResolver()
  { this.parent=null;
  }
  
  public SaxPrefixResolver(PrefixResolver parent)
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
  
  public void mapPrefix(String prefix,URI uri)
  { map.put(prefix,uri);
  }
}
