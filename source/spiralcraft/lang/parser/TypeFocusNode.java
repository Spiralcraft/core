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
import spiralcraft.lang.Reflector;

import spiralcraft.lang.BindException;
import java.net.URI;
import java.util.HashSet;

import spiralcraft.log.ClassLogger;
import spiralcraft.lang.TypeModel;

/**
 * An expression node which resolves a Focus from somewhere in the hierarchy
 *   using a qualified name (ie. <code>[<I>namespace</I>:<I>name</I>]</code>)
 * 
 * @author mike
 *
 */
public class TypeFocusNode
  extends FocusNode
{
  @SuppressWarnings("unused")
  private static final ClassLogger log
    =ClassLogger.getInstance(TypeFocusNode.class);
  
  private static final URI NULL_URI=URI.create("");
  
  private final String suffix;
  private final String namespace;
  private URI uri;

  public TypeFocusNode(String qname)
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
        { namespaceURI=resolver.getDefaultNamespaceURI();
        }
        else
        { namespaceURI=resolver.resolveNamespace(namespace);
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
    
    HashSet<TypeModel> systems=new HashSet<TypeModel>();
    for (Focus<?> chainFocus: focus.getFocusChain())
    {
      if (focus.getSubject()!=null)
      { 
        TypeModel model=chainFocus.getSubject().getReflector().getTypeModel();
        if (!systems.contains(model))
        { systems.add(model);
        }
      }
    }
    
    Reflector<?> reflector=null;
    for (TypeModel model : systems)
    { 
      reflector=model.findType(uri);
      if (reflector!=null)
      { break;
      }
    }
    
    Focus<?> newFocus=null;
    if (reflector!=null)
    { newFocus=focus.chain(reflector.getSelfChannel());
    }
    
    if (newFocus!=null)
    { return newFocus;
    }
    else
    { throw new BindException("Type '"+uri+"' not found. "+focusChain(focus));
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
  { 
    StringBuilder buf=new StringBuilder();
    buf.append("[");
    while (focus!=null)
    { 
      buf.append("\r\n");
      buf.append(focus.toString());
      focus=focus.getParentFocus();
    }
    buf.append("\r\n");
    buf.append("]");
    return buf.toString();
  }
  
  
}
