//
//Copyright (c) 1998,2010 Michael Toth
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

import spiralcraft.lang.FocusChainObject;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.common.Lifecycle;

/**
 * <p>Associates application specific functionality 
 *   with a path in a contextual Resource tree, e.g. to provide remote 
 *   mount points, Translator overlays, metadata associations, debugging
 *   shims, etc.  
 * </p>
 * 
 * <p>A Graft is long lived and contextually bound. 
 * </p>
 * 
 * @author mike
 *
 */
public interface Graft
  extends FocusChainObject,Lifecycle
{

  /**
   * <p>Resolve a Resource relative to this
   *   Graft point's virtual URI.
   * </p>
   * 
   * <p>The provided URI and its path are relative to the Graft
   *   point- ie. the path specified will be interpreted as a descendant
   *   of this Graft point.
   * </p>
   * 
   * <p>The URI must be relative, with no scheme or authority, and a path
   *   that does not begin with "/"
   * </p>
   * @return
   */
  Resource resolve(URI relativePath)
    throws UnresolvableURIException;
  
  
  
  /**
   * <p>The relative URI to be used as a prefix match.
   * </p>
   * 
   * <p>The URI must be relative, with no scheme or authority, and
   *   a path that does not begin with "/"
   * </p>
   * 
   * @return
   */
  URI getVirtualURI();

  
  
}
