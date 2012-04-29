//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.vfs.util;

import java.net.URI;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFactory;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.spi.AbstractResource;

public class NullResource
  extends AbstractResource
{
  public static class Factory
    implements ResourceFactory
  {
    @Override
    public boolean handlesScheme(
      String scheme)
    { return scheme.equals("null");
    }

    @Override
    public Resource resolve(
      URI uri)
      throws UnresolvableURIException
    { return new NullResource();
    } 
  }
  
  public NullResource()
  { super(URI.create("null:/"));
  }

}
