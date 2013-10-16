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
package spiralcraft.data.sax;

import java.net.URI;

import spiralcraft.util.refpool.URIPool;

/**
 * Maps a namespace prefix to a URI
 * 
 * @author mike
 *
 */
public class PrefixMapping
{

  private String prefix;
  private URI uri;
  
  public PrefixMapping()
  { }
  
  public PrefixMapping(String shortHand)
  { 
    String[] prefixValue=shortHand.split("=");
    prefix=prefixValue[0];
    uri=URIPool.create(prefixValue[1]);
    if (prefixValue.length>2)
    { throw new IllegalArgumentException("Too many '=' in "+shortHand);
    }
    
  }
  
  public String getPrefix()
  { return prefix;
  }
  
  public URI getURI()
  { return uri;
  } 
  
  public void setPrefix(String prefix)
  { this.prefix=prefix;
  }
  
  public void setURI(URI uri)
  { this.uri=uri;
  }
  
}
