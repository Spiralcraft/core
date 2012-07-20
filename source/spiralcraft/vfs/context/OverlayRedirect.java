//
//Copyright (c) 2012 Michael Toth
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

import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.ovl.OverlayResource;

/**
 * A Redirect that returns an overlay
 * 
 * @author mike
 *
 */
public class OverlayRedirect
  extends Redirect
{

  private URI overlayURI;
  
  public OverlayRedirect()
  { super();
  }
  
  public OverlayRedirect(URI virtualURI,URI overlayURI,URI baseURI)
  { 
    super(virtualURI,baseURI);
    this.overlayURI=overlayURI;
  }
  
  @Override
  public Resource resolve(URI relativePath)
    throws UnresolvableURIException
  { 
    return new OverlayResource
      (overlayURI.resolve(relativePath)
      ,Resolver.getInstance().resolve(overlayURI.resolve(relativePath))
      ,Resolver.getInstance().resolve(getNewBaseURI().resolve(relativePath))
      );
  }  
}
