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
package spiralcraft.vfs.bundle;

//
//import spiralcraft.log.ClassLog;
//import spiralcraft.log.Level;
import java.io.IOException;
import java.net.URI;

import spiralcraft.bundle.Bundle;
import spiralcraft.bundle.Library;
import spiralcraft.util.Path;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.util.ResourceWrapper;


/**
 * <p>References resources in a bundle library
 * </p>
 * 
 */
public class BundleResource
  extends ResourceWrapper
  implements Resource,Container
{

  private final Resource bundleResource;
  private final URI uri;
  
  public BundleResource(URI uri)
    throws UnresolvableURIException
  { 
    this.uri=uri;
    Library library=Library.get();
    if (library==null)
    { throw new UnresolvableURIException(uri,"No package library available");
    }
    String authority=uri.getAuthority();
    if (authority==null || authority.isEmpty())
    { 
      throw new IllegalArgumentException
        ("URI must have an authority component: "+uri);
    }
    Bundle bundle=library.getBundle(authority);
    if (bundle!=null)
    { this.bundleResource=bundle.getContainer().resolve(Path.create(uri.getPath().substring(1)));
    }
    else
    { throw new UnresolvableURIException(uri,"Bundle not found");
    }
    
  }
  
  protected BundleResource(URI uri,Resource bundleResource)
  {
    this.bundleResource=bundleResource;
    this.uri=uri;
  }
  
  @Override
  public URI getURI()
  { return uri;
  }
  
  @Override
  protected Resource getDelegate()
  { return bundleResource;
  }
  
  @Override
  public Resource[] listContents()
    throws IOException
  { return listChildren();
  }

  @Override
  public Resource[] listChildren()
    throws IOException
  { return listChildren(null);
  }

  @Override
  public Resource[] listChildren(
    ResourceFilter filter)
    throws IOException
  {
    Container bundleContainer=bundleResource.asContainer();
    if (bundleContainer!=null)
    { 
      Resource[] bundleChildren
        =filter!=null
          ?bundleContainer.listChildren(filter)
          :bundleContainer.listChildren()
          ;
          
      Resource[] children=new Resource[bundleChildren.length];
      int i=0;
      for (Resource bundleChild:bundleChildren)
      { 
        children[i++]
          =new BundleResource
            (URIUtil.addPathSegment(uri,bundleChild.getLocalName())
            ,bundleChild
            );
      }
      return children;
    }
    return null;
  }

  @Override
  public Resource[] listLinks()
    throws IOException
  { return null;
  }

  @Override
  public Resource getChild(
    String name)
    throws UnresolvableURIException
  { 
    Container bundleContainer=bundleResource.asContainer();
    if (bundleContainer!=null)
    { 
      Resource child=bundleContainer.getChild(name);
      if (child!=null)
      { return new BundleResource(URIUtil.addPathSegment(uri,name),child);
      }
    }
    return null;
  }

  @Override
  public Container ensureChildContainer(
    String name)
    throws IOException
  { return null;
  }

  @Override
  public Resource createLink(
    String name,
    Resource resource)
    throws UnresolvableURIException
  { return null;
  }

  @Override
  public boolean isContextual()
  { return false;
  }

 
  @Override
  public String toString()
  { return getURI().toString();
  }
 
}
