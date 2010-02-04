//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.vfs.context;


import java.util.HashMap;
import java.util.Map;
import java.net.URI;

/**
 * <P>Provides a mechanism for scoping ContextResource authority names to an
 *   executing thread. Components that wish to define a context://mycontext/
 *   URI should follow the following code example:
 *   
 * <P>-- On configuration<BR>
 *   
 * <P><CODE><PRE>
 *   ContextResourceMap map = new ContextResourceMap();
 *   URI myURI = URI.create("file:/home/myhome/somedir");
 *   map.put("mycontext",myuri);
 *   </PRE></CODE>
 *   
 * <P>-- Where needed:<BR>
 *     
 * <P><CODE><PRE>
 *   map.push();
 *   try
 *   { doSomeWork();
 *   }
 *   finally
 *   { map.pop();
 *   }
 *   </PRE></CODE>
 */
public class ContextResourceMap
{
  
  
  private static final InheritableThreadLocal<ContextResourceMap> threadMap
    =new InheritableThreadLocal<ContextResourceMap>();
  private static volatile int ID;

  static 
  { new ContextResourceMap().push();
  }
  
  public static final URI lookup(String name)
  { return threadMap.get().get(name);
  }
  
  public static final URI getDefault()
  { return threadMap.get().get("_DEFAULT");
  }
  
  public static final Map<String,URI> getMap()
  { return threadMap.get().map;
  }
  
  
  public static final String getMapId()
  {
    StringBuffer out=new StringBuffer();
    ContextResourceMap map=threadMap.get();
    while (map!=null)
    { out.append("/"+map.id);
    }
    return out.toString();
  }
  
  private final HashMap<String,URI> map
    =new HashMap<String,URI>();
  
  private ContextResourceMap parent;
  private final int id=ID++;


  public void putDefault(URI uri)
  { put("_DEFAULT",uri);
  }
  
  public void put(String name,URI uri)
  { map.put(name,uri);
  }

  
  public URI get(String name)
  { 
    URI ret=map.get(name);
    if (ret==null && parent!=null)
    { ret=parent.get(name);
    }
    return ret;
  }
  
  public void push()
  { 
    parent=threadMap.get();
    threadMap.set(this);
  }
  
  public void pop()
  { 
    if (parent!=null)
    { threadMap.set(parent);
    }
    else
    { threadMap.remove();
    }
  }
  
}
