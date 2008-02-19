package spiralcraft.sax;

import java.util.HashMap;

/**
 * Resolves namespace prefixes in a memory efficient manner
 * 
 * @author mike
 */
public class PrefixResolver
{
  private final PrefixResolver parent;
  private final HashMap<String,String> map
    =new HashMap<String,String>();
  
  public PrefixResolver()
  { this.parent=null;
  }
  
  public PrefixResolver(PrefixResolver parent)
  { this.parent=parent;
  }
  
  public String resolvePrefix(String prefix)
  {
    String uri=map.get(prefix);
    if (uri!=null)
    { return uri;
    } 
    else if (parent!=null)
    { return parent.resolvePrefix(prefix);
    }
    return null;
  }
  
  public void mapPrefix(String prefix,String uri)
  { map.put(prefix,uri);
  }
}
