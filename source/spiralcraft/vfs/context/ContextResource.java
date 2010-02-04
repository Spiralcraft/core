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


import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.util.ResourceWrapper;

import java.net.URI;


/**
 * <p>A named virtual resource backed by a physical resource. Used to simplify
 *   and make more portable components which access external resources.
 * </p>
 * 
 * <p>A ContextResource is a simple wrapper for another resource. All methods,
 *   including 'getURI()', delegate to the specific resource.
 * </p>
 * 
 * 
 * <p>A ContextResource is created when a container binds a name to a URI for
 *   the duration of a method call which invokes user subcomponents. The
 *   container will restore the previous URI bound to the name, if any, before
 *   the Thread returns from the method call.
 * </p>
 *   
 * 
 * <p>Examples:
 * <br/>context://war/WEB-INF/web.xml
 * <br/>context://home/myFile.xyz
 * <br/>context://temp/xyz
 * <br/>context:/dir/from/virtual/root
 * </p>
 */
public class ContextResource
  extends ResourceWrapper
{ 

  private String path;
  private Resource delegate;
    


  
  public ContextResource(URI uri)
    throws UnresolvableURIException
  { 
    path=uri.getPath().substring(1);
    String authority=uri.getAuthority();
    URI root;
    if (authority!=null)
    { 
      root=ContextResourceMap.lookup(authority);
      if (root==null)
      { 
        throw new UnresolvableURIException
          (uri
          ,"Unknown resource context '"+authority+"' for "+uri
          +": mappings="+ContextResourceMap.getMap()
          );
      }
    }
    else
    { 
      root=ContextResourceMap.getDefault();
      if (root==null)
      { 
        throw new UnresolvableURIException
          (uri,"No default resource context for "+uri);
      }
    }
    
    URI target=root.resolve(path);
    delegate=Resolver.getInstance().resolve(target);
    
  }
  
  @Override
  protected Resource getDelegate()
  { return delegate;
  }    
  
  
  
}
