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
package spiralcraft.text.io;

import java.io.InputStream;
import java.io.IOException;

import spiralcraft.vfs.Resource;

/**
 * Represents an File as a CharSequence
 */

//
// XXX Support constructors for non-default Character conversion
// 

public class ResourceCharSequence
  extends InputStreamCharSequence
{
  public ResourceCharSequence(Resource resource)
    throws IOException
  { 
    InputStream in=resource.getInputStream();
    try
    {
      if (in==null)
      { throw new IOException("Could not read "+resource.getURI());
      }
      load(in);
    }
    finally
    { in.close();
    }
  }
  
  
}
