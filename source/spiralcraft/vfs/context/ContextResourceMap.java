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
import java.util.LinkedHashSet;
import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.thread.CycleDetector;
import spiralcraft.util.thread.ThreadLocalStack;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

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
  extends AbstractChainableContext
{
  
  private static final ClassLog log
    =ClassLog.getInstance(ContextResourceMap.class);
  private static final Level debugLevel
    =ClassLog.getInitialDebugLevel(ContextResourceMap.class,null);
  
  private static final ThreadLocalStack<ContextResourceMap> threadStack
    =new ThreadLocalStack<ContextResourceMap>(true);
  
//  private static final InheritableThreadLocal<ContextResourceMap> threadMap
//    =new InheritableThreadLocal<ContextResourceMap>();
  
  private static volatile int ID;
  private static final CycleDetector<CycleRef> cycleDetector
    =new CycleDetector<CycleRef>();
  
  static 
  { new ContextResourceMap().push();
  }
  
  

  
//  public static final URI lookup(String name)
//  { return threadMap.get().get(name);
//  }
  
//  public static final URI getDefault()
//  { return threadMap.get().get("");
//  }
  
//  public static final Map<String,URI> getMap()
//  { return threadMap.get().map;
//  }
  
  public static final Resource resolve(URI contextURI)
    throws UnresolvableURIException
  { 
    if (debugLevel.isTrace())
    { log.trace("Resolving "+contextURI);
    }
    Resource ret=threadStack.get().doResolve(contextURI);
    if (ret!=null)
    { return ret;
    }
    else
    { 
      
      throw new UnresolvableURIException
        (contextURI
        ,"Could not resolve "+contextURI+" : mappings="
        +threadStack.get().computeMappings()
        );
    }
  }
  

  
  public static ContextResourceMap get()
  { return threadStack.get();
  }
  
  public static final String getMapId()
  {
    StringBuffer out=new StringBuffer();
    ContextResourceMap map=threadStack.get();
    while (map!=null)
    { 
      out.append("/"+map.id);
      map=map.parent;
    }
    return out.toString();
  }
  
  private final HashMap<String,Authority> map
    =new HashMap<String,Authority>();
  
  private ContextResourceMap parent;
  private final int id=ID++;


  private void setParent(ContextResourceMap parent)
  {
    if (parent==this)
    { throw new IllegalArgumentException("Parent cannot be self");
    }
    if (this.parent==null)
    { this.parent=parent;
    }
    else
    { throw new IllegalStateException("Cannot change parent of "+this);
    }
  }
  
  public void putDefault(URI uri)
  { put("",uri);
  }
  
  public void put(Authority authority)
  { map.put(authority.getAuthorityName(),authority);
  }
  
  /**
   * Map the root for the specified authority name to the specified URI
   * 
   * @param name
   * @param uri
   */
  public void put(String name,URI uri)
  { map.put(name,new Authority(name,uri));
  }

  
  public URI get(String name)
  { 
    Authority mapping=map.get(name);
    URI ret=mapping!=null?mapping.getRootURI():null;
    if (ret==null && parent!=null)
    { ret=parent.get(name);
    }
    return ret;
  }

  
  @Override
  protected void pushLocal()
  { threadStack.push(this);
  }
  
  @Override
  protected void popLocal()
  { threadStack.pop();
  }
  /**
   * Return a Graft mapping for an authority 
   * 
   * @param authorityName
   * @param relativePath
   * @return
   */
  public Graft getGraft(String authorityName,String relativePath)
  {
    Authority authority=map.get(authorityName);
    if (authority==null)
    { 
      throw new IllegalArgumentException
        ("No authority named '"+authorityName+"' in this FileSpace");
    }
    
    return authority.getGraft(relativePath);
  }
  
  LinkedHashSet<URI> computeMappings()
  { 
    LinkedHashSet<URI> set
      =parent!=null
      ?parent.computeMappings()
      :new LinkedHashSet<URI>();
      
    for (String name:map.keySet())
    { 
      if (name.equals(""))
      { set.add(URI.create("context:/"));
      }
      else
      { set.add(URI.create("context://"+name));
      }
    }
    return set;
  }
  
  private Resource doResolve(URI contextURI)
    throws UnresolvableURIException
  {
    if (cycleDetector.detectOrPush(new CycleRef(this,contextURI)))
    { throw new UnresolvableURIException(contextURI,"Cyclic contextual definition");
    }
    
    try
    {
      if (contextURI.getPath().length()==0)
      { throw new UnresolvableURIException(contextURI,"URI has no path");
      }
      String path=contextURI.getPath().substring(1);
      String authorityName=contextURI.getAuthority();
      if (authorityName!=null && !contextURI.isAbsolute())
      { throw new UnresolvableURIException(contextURI,"Absolute URI has no scheme");
      }
      Authority authority;
  
      if (authorityName!=null)
      { 
        authority=map.get(authorityName);
        if (authority==null && parent==null)
        { return null;
  //        throw new UnresolvableURIException
  //        (contextURI
  //          ,"Unknown context authority '"+authorityName+"' for "+contextURI
  //          +": "+id+" mappings="+map
  //        );
        }
      }
      else
      { 
        authority=map.get("");
        if (authority==null && parent==null)
        { return null;
  //        throw new UnresolvableURIException
  //        (contextURI,"No default context authority for "+contextURI
  //          +": "+id+" mappings="+map
  //        );
        }
      }
  
      if (authority==null)
      { return parent.doResolve(contextURI);
      }
  
  
      Resource ret=authority.resolve(path);
  
      if (ret==null && parent!=null)
      { ret=parent.doResolve(contextURI);
      }
      else
      {
        if (debugLevel.canLog(Level.TRACE))
        { 
          log.trace
          (ContextResourceMap.getMapId()
            +": Resolved "+(ret!=null?ret.getURI():null)
            +" from authority ["+authorityName+"] for "+path
          );
        }
      }
      return ret;
    }
    finally
    { cycleDetector.pop();
    }
  }  
  
  @Override
  public String toString()
  { return super.toString()+": "+map.toString()+" parent="+parent;
  }


  @Override
  public Focus<?> bindImports(
    Focus<?> focusChain)
    throws BindException
  { 
    ContextResourceMap parent=ContextResourceMap.get();
    setParent(parent);
    return focusChain;
  }
  
}

class CycleRef
{
  private final URI uri;
  private final ContextResourceMap map;
  private int hash;
   
  public CycleRef(ContextResourceMap map,URI uri)
  { 
    this.map=map;
    this.uri=uri;
    this.hash=uri.hashCode()*37+map.hashCode();
  }
    
  @Override
  public int hashCode()
  { return hash;
  }
    
  @Override
  public boolean equals(Object other)
  { 
      
    return uri.equals( ((CycleRef) other).uri)
      && map==(((CycleRef) other).map);
  }
}


