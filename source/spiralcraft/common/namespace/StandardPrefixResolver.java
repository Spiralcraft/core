//
// Copyright (c) 2008,20010 Michael Toth
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
package spiralcraft.common.namespace;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


/**
 * Resolves namespace prefixes in a memory efficient manner
 * 
 * @author mike
 */
public class StandardPrefixResolver
  implements PrefixResolver
{
  private final PrefixResolver parent;
  private final HashMap<String,URI> map
    =new HashMap<String,URI>();
  
  public StandardPrefixResolver()
  { this.parent=null;
  }
  
  public StandardPrefixResolver(PrefixResolver parent)
  { this.parent=parent;
  }
  
  @Override
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

  @Override
  public Map<String,URI> computeMappings()
  { 
    Map<String,URI> computedMappings=new HashMap<String,URI>();

    Map<String,URI> parentMappings=parent!=null?parent.computeMappings():null;
    if (parentMappings!=null)
    { computedMappings.putAll(parentMappings);
    }
    computedMappings.putAll(map);
    return computedMappings;
  }
  
  public Map<String,URI> getMappings()
  { return map;
  }
  
  public void setMappings(Map<String,URI> mappings)
  { 
    for (Map.Entry<String,URI> entry: map.entrySet())
    { mapPrefix(entry.getKey(),entry.getValue());
    }
  }
}
