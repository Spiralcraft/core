//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.common.namespace;

import java.net.URI;

import spiralcraft.common.Immutable;
import spiralcraft.util.string.StringUtil;

/**
 * <p>A prefixed name, which consists of an optional namespace prefix and
 *   a local name. 
 * </p>
 * 
 * @author mike
 *
 */
@Immutable
public class PrefixedName
{

  private final String prefix;
  private final String localName;
  
  public PrefixedName(String canonicalForm)
  { 
    String[] pair=StringUtil.explode(canonicalForm,':',(char) 0,2);
    switch (pair.length)
    {
      case 1:
        prefix=null;
        localName=pair[0];
        break;
      case 2:
        prefix=pair[0];
        localName=pair[1];
        break;
      default:
        throw new IllegalArgumentException("Too many colons in prefixedName");
    }    
  }
  
  public PrefixedName(String prefix,String localName)
  { 
    this.prefix=prefix;
    this.localName=localName;
  }
  
  public String getPrefix()
  { return prefix;
  }
  
  public String getLocalName()
  { return localName;
  }
  
  @Override
  public String toString()
  { return prefix!=null?(prefix+":"+localName):localName;
  }
  
  public QName resolve()
    throws UnresolvedPrefixException
  { return resolve(NamespaceContext.getPrefixResolver());
  }
  
  public QName resolve(PrefixResolver resolver)
    throws UnresolvedPrefixException
  { 
    if (resolver==null)
    { 
      throw new IllegalArgumentException
        ("No resolver for "+this);
    }
    
    if (prefix!=null)
    { 
      URI uri=resolver.resolvePrefix(prefix);
      if (uri==null)
      { 
        throw new UnresolvedPrefixException
          (prefix,localName,resolver);
      }
      return new QName(uri,localName);
    }
    else
    { 
      URI uri=resolver.resolvePrefix("");
      return new QName(uri,localName);
    }
  }
}
