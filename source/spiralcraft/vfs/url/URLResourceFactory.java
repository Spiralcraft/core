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
package spiralcraft.vfs.url;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFactory;
import spiralcraft.vfs.UnresolvableURIException;

import java.net.URI;

public class URLResourceFactory
  implements ResourceFactory

{

  @Override
  public Resource resolve(URI uri)
    throws UnresolvableURIException
  { return new URLResource(uri);
  }


  @Override
  public boolean handlesScheme(String scheme)
  { 
    return scheme.equals("http")
        || scheme.equals("https")
        || scheme.equals("ftp")
        || scheme.equals("file")
        || scheme.equals("jar");
  }

}
