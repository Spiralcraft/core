//
//Copyright (c) 2010 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.vfs.context;

import java.net.URI;

import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

/**
 * <p>A Graft that resolves a path under a root 
 * </p>
 * 
 * @author mike
 *
 */
public class Redirect
  implements Graft
{
  private URI virtualURI;
  private URI baseURI;
  
  
  @Override
  public URI getVirtualURI()
  { return virtualURI;
  }
  
  
  public void setVirtualURI(URI virtualURI)
  { this.virtualURI=virtualURI;
  }

  public URI getNewBaseURI()
  { return baseURI;
  }
  
  
  public void setNewBaseURI(URI baseURI)
  { this.baseURI=baseURI;
  }

  @Override
  public Resource resolve(URI relativePath)
    throws UnresolvableURIException
  { return Resolver.getInstance().resolve(baseURI.resolve(relativePath));
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }

  @Override
  public void start()
    throws LifecycleException
  {

  }

  @Override
  public void stop()
    throws LifecycleException
  {

  }

}
