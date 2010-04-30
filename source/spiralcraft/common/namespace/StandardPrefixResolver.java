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
