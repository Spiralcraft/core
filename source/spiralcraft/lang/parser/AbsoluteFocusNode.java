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

import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.common.namespace.UnresolvedPrefixException;
import spiralcraft.lang.Focus;

import spiralcraft.lang.BindException;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.lang.ClassUtil;
import spiralcraft.util.refpool.URIPool;

import java.net.URI;


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
  
  
  private final String suffix;
  private final String namespace;
  private URI uri;

  public AbsoluteFocusNode(String qname)
    throws UnresolvedPrefixException
  { 
    int colonPos=qname.indexOf(':');
    if (colonPos==0)
    { 
      this.uri=URIPool.create(qname.substring(1));
      this.namespace=null;
      this.suffix=null;
    }
    else if (colonPos>0)
    {
      this.namespace=qname.substring(0,colonPos);
      this.suffix=qname.substring(colonPos+1);
      this.uri=resolveQName(namespace,suffix);
    }
    else
    { 
      this.namespace=null;
      this.suffix=qname;
      this.uri=resolveQName(namespace,suffix);
    }
    hashCode=computeHashCode();
  }
  
  public AbsoluteFocusNode(String suffix,String namespace,URI uri)
  { 
    this.suffix=suffix;
    this.namespace=namespace;
    this.uri=uri;
    hashCode=computeHashCode();
  }

  @Override
  public Node[] getSources()
  { return null;
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    if (this.uri!=null)
    { return this;
    }
    
    URI uri=null;
    if (visitor instanceof PrefixResolver && suffix!=null)
    { uri=resolveQName(namespace,suffix,(PrefixResolver) visitor);
    }
    
    if (uri!=null)
    { return new AbsoluteFocusNode(null,null,uri);
    }
    else
    { return this;
    }
  } 
  
  @Override
  public String reconstruct()
  { 
    if (namespace!=null)
    { return "["+namespace+":"+suffix+"]";
    }
    else if (suffix!=null)
    { return "["+suffix+"]";
    }
    else if (uri!=null)
    { return "[:"+uri+"]";
    }
    else
    { return "[]";
    }
  }
  
  @Override
  public Focus<?> findFocus(final Focus<?> focus)
    throws BindException
  { 
    
    if (uri==null)
    {
      PrefixResolver resolver=focus.getNamespaceResolver();
      if (resolver!=null)
      { uri=resolveQName(namespace,suffix,resolver);
      }
      else if (namespace!=null)
      { 
        throw new BindException
          ("No NamespaceResolver for namespace '"+namespace+"' in \r\n"
            +focus.getFocusChain().toString());
      }
      else
      {
        throw new BindException
          ("No NamespaceResolver to provide default namespace for '"
            +suffix+"' in \r\n"
            +focus.getFocusChain().toString());

      }

      if (uri==null)
      { 
        if (namespace!=null)
        {
          throw new BindException
            ("Namespace '"+namespace+"' not defined in \r\n"
              +focus.getFocusChain().toString()
            );
        }
        else
        {
          throw new BindException
            ("Default namespace for suffix '"+suffix+"' not defined in \r\n"
              +focus.getFocusChain().toString()
            );
        }
      }

      // log.fine(namespaceURI.toString()+"  :  "+suffix);
    }
    
    Focus<?> newFocus=focus.findFocus(uri);
    if (newFocus!=null)
    { return newFocus;
    }
    else
    { throw new BindException("Focus '"+uri+"' not found."+focusChain(focus));
    }
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Focus");
    prefix=prefix+"  ";
    out.append(prefix).append("namespace="+(namespace!=null?namespace:"(default)"));
    if (suffix!=null)
    { out.append(prefix).append("name="+suffix);
    }
  }
  
  private String focusChain(Focus<?> focus)
  { return focus.getFocusChain().toString();
  }

  private int computeHashCode()
  { 
    return ArrayUtil.arrayHashCode(new Object[] {suffix,namespace,uri});
  }
  
  @Override
  protected boolean equalsNode(Node node)
  {
    AbsoluteFocusNode mynode=(AbsoluteFocusNode) node;
    return ClassUtil.equals(suffix,mynode.suffix)
      && ClassUtil.equals(namespace,mynode.namespace)
      && ClassUtil.equals(uri,mynode.uri);
  }
  
}
