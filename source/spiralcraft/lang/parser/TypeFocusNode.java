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
import spiralcraft.lang.Reflector;

import spiralcraft.lang.BindException;
import java.net.URI;

import spiralcraft.lang.TypeModel;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.lang.ClassUtil;

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
  
  private final String suffix;
  private final String namespace;
  private URI uri;
  private final int arrayDepth;

  public TypeFocusNode(String qname,int arrayDepth)
    throws UnresolvedPrefixException
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
      this.uri=resolveQName(namespace,suffix);
    }
    else
    { 
      this.namespace=null;
      this.suffix=qname;
      this.uri=resolveQName(namespace,suffix);
    }
    this.arrayDepth=arrayDepth;
    hashCode=computeHashCode();
  }

  TypeFocusNode(String suffix,String namespace,URI uri,int arrayDepth)
  { 
    this.suffix=suffix;
    this.namespace=namespace;
    this.uri=uri;
    this.arrayDepth=arrayDepth;
    hashCode=computeHashCode();
  }
  
  @Override
  public Node[] getSources()
  { return null;
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    URI uri=null;
    if (visitor instanceof PrefixResolver && suffix!=null)
    { uri=resolveQName(namespace,suffix,(PrefixResolver) visitor);
    }
    
    if (uri!=null)
    { return new TypeFocusNode(null,null,uri,arrayDepth);
    }
    else
    { return new TypeFocusNode(suffix,namespace,this.uri,arrayDepth);
    }
  } 
  
  @Override
  public String reconstruct()
  { 
    String txt;
    if (namespace!=null)
    { txt="[@"+namespace+":"+suffix+"]";
    }
    else if (uri!=null)
    { txt="[@:"+uri+"]";
    }
    else
    { txt="[@"+suffix+"]";
    }
    for (int i=0;i<arrayDepth;i++)
    { txt=txt+"[]";
    }    
    return txt;
  }
  
  @Override
  public Focus<?> findFocus(final Focus<?> focus)
    throws BindException
  {     
    if (uri==null)
    {
      PrefixResolver resolver=focus.getNamespaceResolver();
      if (resolver!=null)
      {
        uri=resolveQName(namespace,suffix,resolver);
        
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

      // log.fine(uri.toString()+"  :  "+namespace+":"+suffix);
    }
    
    
    Reflector<?> reflector=TypeModel.searchType(uri);
    
    
    
    Focus<?> newFocus=null;
    if (reflector!=null)
    { 
      for (int i=0;i<arrayDepth;i++)
      { reflector=ArrayReflector.getInstance(reflector);
      }
      
      newFocus=focus.chain(reflector.getSelfChannel());
    }
    
    if (newFocus!=null && newFocus.getSubject()!=null)
    { return newFocus;
    }
    else
    { throw new BindException
        ("Type '"+uri+"' not found. Searched TypeModels: "
          +ArrayUtil.format(TypeModel.getRegisteredModels(),",","")
        );
    }
  }
  
  public TypeFocusNode arrayType()
  { return new TypeFocusNode(suffix,namespace,uri,arrayDepth+1);
  }

  private int computeHashCode()
  { 
    return (ArrayUtil.arrayHashCode(new Object[] {suffix,namespace,uri}) *31)
      +arrayDepth;
  }
  
  @Override
  protected boolean equalsNode(Node node)
  {
    TypeFocusNode mynode=(TypeFocusNode) node;
    return ClassUtil.equals(suffix,mynode.suffix)
      && ClassUtil.equals(namespace,mynode.namespace)
      && ClassUtil.equals(uri,mynode.uri)
      && arrayDepth==mynode.arrayDepth;
  }  
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("TypeFocus");
    prefix=prefix+"  @";
    out.append(prefix).append("namespace="+(namespace!=null?namespace:"(default)"));
    if (suffix!=null)
    { out.append(prefix).append("name="+suffix);
    }
    for (int i=0;i<arrayDepth;i++)
    { out.append("[]");
    }
  }
  

  
  
}
