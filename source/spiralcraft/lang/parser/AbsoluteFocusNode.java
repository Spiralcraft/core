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
package spiralcraft.lang.parser;

import spiralcraft.lang.Focus;
import spiralcraft.lang.NamespaceResolver;

import spiralcraft.lang.BindException;
import java.net.URI;

import spiralcraft.log.ClassLogger;

/**
 * An expression node which resolves a Focus from somewhere in the hierarchy
 *   using a qualified name (ie. <code>[<I>namespace</I>:<I>name</I>]</code>)
 * 
 * @author mike
 *
 */
public class AbsoluteFocusNode
  extends FocusNode
{
  @SuppressWarnings("unused")
  private static final ClassLogger log=new ClassLogger(AbsoluteFocusNode.class);
  
  private static final URI NULL_URI=URI.create("");
  
  private final String suffix;
  private final String namespace;

  public AbsoluteFocusNode(String qname)
  { 
    int colonPos=qname.indexOf(':');
    if (colonPos>-1)
    {
      this.namespace=qname.substring(0,colonPos);
      this.suffix=qname.substring(colonPos+1);
    }
    else
    { 
      this.namespace=null;
      this.suffix=qname;
    }

  }

  public Focus<?> findFocus(final Focus<?> focus)
    throws BindException
  { 
    URI namespaceURI=NULL_URI;
    NamespaceResolver resolver=focus.getNamespaceResolver();
    if (resolver!=null)
    {
      if (namespace==null || namespace.equals(""))
      { namespaceURI=resolver.getDefaultNamespaceURI();
      }
      else
      { namespaceURI=resolver.resolveNamespace(namespace);
      }
    }
    else if (namespace!=null)
    { 
      throw new BindException
        ("No NamespaceResolver for namespace '"+namespace+"'");
    }
    
    if (namespaceURI==null)
    { throw new BindException("Namespace '"+namespace+"' not defined.");
    }
    
    // log.fine(namespaceURI.toString()+"  :  "+suffix);
    URI uri=namespaceURI.resolve(suffix);
    
    Focus<?> newFocus=focus.findFocus(uri);
    if (newFocus!=null)
    { return newFocus;
    }
    else
    { throw new BindException("Focus '"+uri+"' not found.");
    }
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Focus");
    prefix=prefix+"  ";
    out.append(prefix).append("namespace="+(namespace!=null?namespace:"(default)"));
    if (suffix!=null)
    { out.append(prefix).append("name="+suffix);
    }
  }
  
}
