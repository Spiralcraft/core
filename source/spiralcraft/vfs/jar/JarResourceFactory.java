//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.vfs.jar;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFactory;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.vfs.url.URLResource;

import java.net.URI;

public class JarResourceFactory
  implements ResourceFactory

{

  @Override
  public Resource resolve(URI uri)
    throws UnresolvableURIException
  { 
    if (URI.create(uri.getRawSchemeSpecificPart()).getScheme().equals("file"))
    { return new JarFileResource(uri);
    }
    else
    { return new URLResource(uri);
    }
  }

  @Override
  public boolean handlesScheme(String scheme)
  { return scheme.equals("jar");
  }

}
