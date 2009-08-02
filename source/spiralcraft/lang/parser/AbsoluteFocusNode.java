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

import spiralcraft.common.NamespaceResolver;
import spiralcraft.lang.Focus;

import spiralcraft.lang.BindException;
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
  
  private static final URI NULL_URI=URI.create("");
  
  private final String suffix;
  private final String namespace;
  private URI uri;

  public AbsoluteFocusNode(String qname)
  { 
    int colonPos=qname.indexOf(':');
    if (colonPos==0)
    { 
      this.uri=URI.create(qname.substring(1));
      this.namespace=null;
      this.suffix=null;
    }
    else if (colonPos>0)
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
  
  public AbsoluteFocusNode(String suffix,String namespace,URI uri)
  { 
    this.suffix=suffix;
    this.namespace=namespace;
    this.uri=uri;
  }

  @Override
  public Node[] getSources()
  { return null;
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    URI uri=null;
    if (visitor instanceof NamespaceResolver && suffix!=null)
    {
      NamespaceResolver nsr=(NamespaceResolver) visitor;
      if (namespace!=null)
      { uri=nsr.resolvePrefix(namespace);
      }
      else
      { uri=nsr.getDefaultURI();
      }
      if (uri!=null)
      { 
        String uriStr=uri.toString();
        if (!uriStr.endsWith("/"))
        { 
          uriStr=uriStr+"/";
          uri=URI.create(uriStr);
        }
        uri=uri.resolve(suffix);
      }
    }
    
    if (uri!=null)
    { return new AbsoluteFocusNode(null,null,uri);
    }
    else
    { return new AbsoluteFocusNode(suffix,namespace,this.uri);
    }
  } 
  
  @Override
  public String reconstruct()
  { 
    if (namespace!=null)
    { return "["+namespace+":"+suffix+"]";
    }
    else if (uri!=null)
    { return "[:"+uri+"]";
    }
    else
    { return "["+suffix+"]";
    }
  }
  
  @Override
  public Focus<?> findFocus(final Focus<?> focus)
    throws BindException
  { 
    URI namespaceURI=NULL_URI;
    
    if (uri==null)
    {
      NamespaceResolver resolver=focus.getNamespaceResolver();
      if (resolver!=null)
      {
        if (namespace==null || namespace.equals(""))
        { namespaceURI=resolver.getDefaultURI();
        }
        else
        { namespaceURI=resolver.resolvePrefix(namespace);
        }
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

      if (namespaceURI==null)
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
      uri=namespaceURI.resolve(suffix);
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


  
  
}
