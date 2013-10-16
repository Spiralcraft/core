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
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.util.string.StringUtil;

/**
 * <p>A qualified name, which consists of a namespace URI and a local 
 *   name.
 * </p>
 * 
 * @author mike
 *
 */
@Immutable
public class QName
{
  private final URI namespaceURI;
  private final String localName;

  
  /**
   * Resolve a name in one of the following forms:
   * 
   *   1. localName
   *   2. namespacePrefix+":"+localName using the contextual PrefixResolver.
   *   3. {uri}localName fully qualified form
   * 
   * @param prefixedName
   * @return a QName
   */
  public QName resolve(String prefixedName)
    throws UnresolvedPrefixException
  { return resolve(prefixedName,NamespaceContext.getPrefixResolver());
  }
  
  /**
   * Resolve a name in one of the following forms:
   * 
   *   1. localName
   *   2. namespacePrefix+":"+localName using the specified PrefixResolver.
   *   3. {uri}localName fully qualified form
   * 
   * @param prefixedName
   * @return a QName
   */
  public static QName resolve(String prefixedName,PrefixResolver resolver)
    throws UnresolvedPrefixException
  { 
    if (prefixedName.startsWith("{"))
    { return new QName(prefixedName);
    }
        
    String[] pair=StringUtil.explode(prefixedName,':',(char) 0,2);
    switch (pair.length)
    {
      case 1:
        return new QName(null,pair[0]);
      case 2:
        if (resolver==null)
        { throw new IllegalArgumentException
            ("No resolver to resolver "+prefixedName);
        }
        URI uri=resolver.resolvePrefix(pair[0]);
        if (uri==null)
        { throw new UnresolvedPrefixException(pair[0],pair[1],resolver);
        }
        return new QName(uri,pair[1]);
      default:
        throw new IllegalArgumentException("Too many colons in prefixedName");
    }
  }
  
  /**
   * Construct a QName from the canonical form using the "{uri}name" syntax 
   *   defined in http://jclark.com/xml/xmlns.htm
   *    
   * @param canonicalForm
   */
  public QName(String canonicalForm)
  {
    if (canonicalForm.startsWith("{"))
    { 
      int endBracket=canonicalForm.indexOf('}');
      namespaceURI=URIPool.create(canonicalForm.substring(1,endBracket));
      localName=canonicalForm.substring(endBracket+1);
    }
    else
    {
      namespaceURI=null;
      localName=canonicalForm;
    }
     
  }
  
  public QName(URI namespaceURI,String localName)
  { 
    this.namespaceURI=namespaceURI;
    this.localName=localName;
  }
  
  public URI getNamespaceURI()
  { return namespaceURI;
  }
  
  public String getLocalName()
  { return localName;
  }
  
  /**
   * Generate the canonical form using the "{uri}name" syntax
   *   defined in http://jclark.com/xml/xmlns.htm
   */
  @Override
  public String toString()
  { 
    if (namespaceURI!=null)
    { return "{"+namespaceURI+"}"+localName;
    }
    else
    { return localName;
    }
  }

  /**
   * Resolve the localName as the final path segment of the hierarchical
   *   namespace URI. Will add a "/" to the URI if it is not present.
   * 
   * @return
   */
  public URI toURIPath()
  {
    if (namespaceURI==null)
    { return URIPool.create(localName);
    }
    else
    { return URIUtil.addPathSegment(namespaceURI,localName);
    }
  }
}
