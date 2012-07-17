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
package spiralcraft.app;

import java.net.URI;

import spiralcraft.util.Path;
import spiralcraft.util.PathPattern;
import spiralcraft.util.URIUtil;

/**
 * Maps a Filter to a URL path segment
 * 
 * @author mike
 *
 */
public class PathContextMapping
{
  PathPattern[] patterns;
  URI contextURI;
  
  public void setPatterns(PathPattern[] patterns)
  { this.patterns=patterns;
  }
  
  public boolean matches(String path)
  { 
    if (patterns!=null)
    {
      for (PathPattern pattern:patterns)
      { 
        if (pattern.toString().equals(path) 
            || pattern.matches(Path.create(path))
            )
        { return true;
        }
      }
      return false;
    }
    else
    { return true;
    }
    
  }
  
  public void setContextURI(URI contextURI)
  { this.contextURI=URIUtil.ensureTrailingSlash(contextURI);
  }
  
  public URI getContextURI()
  { return contextURI;
  }

}
