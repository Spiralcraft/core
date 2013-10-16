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
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

import spiralcraft.lang.BindException;
import spiralcraft.util.refpool.URIPool;

import java.net.URI;

/**
 * Creates a single instance of the type referenced by the specified URI for
 *   a given bound Channel.
 * 
 * @author mike
 *
 */
public class ChannelMetaNode<Tsource,Treturn>
  extends Node
{
  
  private final String suffix;
  private final String namespace;
  private URI uri;
  private Node source;

  public ChannelMetaNode
    (Node source
    ,String qname
    )
    throws UnresolvedPrefixException
  { 
    this.source=source;
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

  }

  ChannelMetaNode
    (Node source
    ,String suffix
    ,String namespace
    ,URI uri
    )
  { 
    this.source=source;
    this.suffix=suffix;
    this.namespace=namespace;
    this.uri=uri;
  }
  
  @Override
  public Node[] getSources()
  { return new Node[] {source};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    boolean dirty=false;
    
    URI uri=this.uri;
    if (uri==null && visitor instanceof PrefixResolver && suffix!=null)
    { uri=resolveQName(namespace,suffix,(PrefixResolver) visitor);
    }
    if (uri!=this.uri)
    { dirty=true;
    }
    
    Node sourceCopy=source.copy(visitor);
    if (sourceCopy!=this)
    { dirty=true;
    }
    
    if (dirty)
    { 
      return new ChannelMetaNode<Tsource,Treturn>
        (source,suffix,namespace,uri);
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
    else if (uri!=null)
    { return "[:"+uri+"]";
    }
    else
    { return "["+suffix+"]";
    }
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Channel<Treturn> bind(final Focus<?> focus)
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
    
    Channel sourceChannel=source.bind(focus);
    Channel ret=sourceChannel.resolveMeta(focus,uri);
    if (ret==null)
    { throw new BindException("No metadata ["+uri+"] available from source: "+sourceChannel);
    }
    return ret;
        
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("ChannelMeta ");
    prefix=prefix+"  ";
    out.append(prefix).append("namespace="+(namespace!=null?namespace:"(default)"));
    if (suffix!=null)
    { out.append(prefix).append("name="+suffix);
    }
  }
  

  
  
}
