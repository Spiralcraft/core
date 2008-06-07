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
 * <P>A named virtual resource backed by a physical resource. Used to simplify
 *   and make more portable components which access external resources.
 * 
 * <P>A ContextResource is a simple wrapper for another resource. All methods,
 *   including 'getURI()', will return the details of the specific resource.
 * 
 * 
 * <P>A ContextResource is created when a container binds a name to a URI for
 *   the duration of a method call which invokes user subcomponents. The
 *   container will restore the previous URI bound to the name, if any, before
 *   the Thread returns from the method call.
 *   
 * 
 * <P>Examples:
 * <BR>context://war/WEB-INF/web.xml
 * <BR>context://home/myFile.xyz
 * <BR>context://temp/xyz
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
    URI root=ContextResourceMap.lookup(authority);
    if (root==null)
    { 
      throw new UnresolvableURIException
        (uri,"Unknown resource context '"+authority+"'");
    }
    URI target=root.resolve(path);
    delegate=Resolver.getInstance().resolve(target);
    
  }
  
  protected Resource getDelegate()
  { return delegate;
  }    
  
  
}
