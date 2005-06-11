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
package spiralcraft.stream.file;

import spiralcraft.stream.ResourceFactory;
import spiralcraft.stream.Resource;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;

public class FileResourceFactory
  implements ResourceFactory

{

  public Resource resolve(URI uri)
    throws UnresolvableURIException
  { return new FileResource(uri);
  }



}
