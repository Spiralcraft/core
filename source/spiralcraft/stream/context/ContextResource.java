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
package spiralcraft.stream.context;

import spiralcraft.stream.UnresolvableURIException;
import spiralcraft.stream.Resolver;

import spiralcraft.stream.util.ResourceWrapper;

import java.net.URI;

import java.util.HashMap;

/**
 * <P>A named virtual resource backed by a physical resource. Used to simplify
 *   and make more portable components which access external resources.
 * 
 * <P>A ContextResource is created when a container binds a name to a URI for
 *   the duration of a method call which invokes user subcomponents. The
 *   container will restore the previous URI bound to the name, if any, before
 *   the Thread returns from the method call.
 * 
 * <P>Examples:
 * <BR>context://war/WEB-INF/web.xml
 * <BR>context://home/myFile.xyz
 * <BR>context://temp/xyz
 */
public class ContextResource
  extends ResourceWrapper
{
  
  private URI uri;
  private String path;
  
  private static final ThreadLocal<HashMap<String,URI>> threadMap
    =new ThreadLocal<HashMap<String,URI>>()
    {
      protected HashMap<String,URI> initialValue()
      { return new HashMap<String,URI>();
      }
    };
    
  
  public static final URI lookup(String name)
  { return threadMap.get().get(name);
  }
  
  public static final void bind(String name,URI uri)
  { threadMap.get().put(name,uri);
  }
  
  public ContextResource(URI uri)
    throws UnresolvableURIException
  { 
    this.uri=uri;
    path=uri.getPath().substring(1);
    String authority=uri.getAuthority();
    URI root=lookup(authority);
    if (root==null)
    { 
      throw new UnresolvableURIException
        (uri,"Unknown resource context '"+authority+"'");
    }
    URI target=root.resolve(path);
    delegate=Resolver.getInstance().resolve(target);
    
  }
  
  
}
