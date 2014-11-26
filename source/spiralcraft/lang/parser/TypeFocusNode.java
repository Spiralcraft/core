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
import spiralcraft.util.refpool.URIPool;

/**
 * An expression node which resolves a Reflector instance from one of the
 *   available type systems
 *   using a qualified name (ie. <code>[@<I>namespace</I>:<I>name</I>]</code>)
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
  private final Node argBlock;

  public TypeFocusNode(String qname,int arrayDepth,Node argBlock)
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
      if (!this.suffix.isEmpty())
      { this.uri=resolveQName(namespace,suffix);
      }
    }
    else
    { 
      this.namespace=null;
      this.suffix=qname;
      if (!this.suffix.isEmpty())
      { this.uri=resolveQName(namespace,suffix);
      }
    }
    this.arrayDepth=arrayDepth;
    this.argBlock=argBlock;
    hashCode=computeHashCode();
  }

  TypeFocusNode(String suffix,String namespace,URI uri,int arrayDepth,Node argBlock)
  { 
    this.suffix=suffix;
    this.namespace=namespace;
    this.uri=uri;
    this.arrayDepth=arrayDepth;
    this.argBlock=argBlock;
    hashCode=computeHashCode();
  }
  
  @Override
  public Node[] getSources()
  { 
    if (argBlock!=null)
    { return new Node[] {argBlock};
    }
    return null;
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    URI uri=null;
    if (visitor instanceof PrefixResolver && suffix!=null && !suffix.isEmpty())
    { uri=resolveQName(namespace,suffix,(PrefixResolver) visitor);
    }
    
    Node argBlockCopy=argBlock.copy(visitor);
    
    if (uri!=null)
    { return new TypeFocusNode(null,null,uri,arrayDepth,argBlockCopy);
    }
    else
    { return new TypeFocusNode(suffix,namespace,this.uri,arrayDepth,argBlockCopy);
    }
  } 
  
  @Override
  public String reconstruct()
  { 
    String txt;
    if (namespace!=null)
    { txt
        ="[@"+namespace+":"+suffix+(argBlock!=null?argBlock.reconstruct():"")
        +"]";
    }
    else if (uri!=null)
    { txt="[@:"+uri+(argBlock!=null?argBlock.reconstruct():"")+"]";
    }
    else
    { txt="[@"+suffix+(argBlock!=null?argBlock.reconstruct():"")+"]";
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
      if (resolver!=null && suffix!=null && !suffix.isEmpty())
      { uri=resolveQName(namespace,suffix,resolver);
      }
      else if (namespace!=null)
      { 
        throw new BindException
          ("No NamespaceResolver for namespace '"+namespace+"' in \r\n"
            +focus.getFocusChain().toString());
      }
      else if (argBlock==null)
      {
        
        throw new BindException
          ("No NamespaceResolver to provide default namespace for '"
            +suffix+"' in \r\n"
            +focus.getFocusChain().toString());

      }

      if (uri==null && argBlock==null)
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
    
    
    Reflector<?> reflector=null;
    if (argBlock==null && uri!=null)
    { reflector=TypeModel.searchType(uri);
    }
    else if (argBlock!=null)
    { 
      reflector=argBlock.bind(focus).getReflector();
      if (reflector==null)
      { throw new BindException("Arg block has no reflector");
      }
    }
    
    
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
  { return new TypeFocusNode(suffix,namespace,uri,arrayDepth+1,argBlock);
  }

  private int computeHashCode()
  { 
    return (ArrayUtil.arrayHashCode(new Object[] {suffix,namespace,uri}) *31)
      +arrayDepth+(argBlock!=null?argBlock.hashCode():0);
  }
  
  @Override
  protected boolean equalsNode(Node node)
  {
    TypeFocusNode mynode=(TypeFocusNode) node;
    return ClassUtil.equals(suffix,mynode.suffix)
      && ClassUtil.equals(namespace,mynode.namespace)
      && ClassUtil.equals(uri,mynode.uri)
      && arrayDepth==mynode.arrayDepth
      && ClassUtil.equals(argBlock,mynode.argBlock);
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
    if (argBlock!=null)
    { 
      out.append(prefix).append("args=");
      argBlock.dumpTree(out,prefix+2);
    }
    for (int i=0;i<arrayDepth;i++)
    { out.append("[]");
    }
  }
  

  
  
}
