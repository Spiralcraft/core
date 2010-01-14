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
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.Reflectable;
import spiralcraft.lang.Reflector;

import spiralcraft.lang.BindException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import spiralcraft.lang.TypeModel;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * An expression node which resolves a Focus from somewhere in the hierarchy
 *   using a qualified name (ie. <code>[<I>namespace</I>:<I>name</I>]</code>)
 * 
 * @author mike
 *
 */
public class ObjectLiteralNode<Tobject,Treturn>
  extends Node
{
  
  private final String suffix;
  private final String namespace;
  private URI uri;
  private Node source;
  private List<Node> paramNodes;

  public ObjectLiteralNode
    (Node source
    ,String qname
    ,List<Node> paramNodes
    )
  { 
    this.source=source;
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
    this.paramNodes=paramNodes;

  }

  ObjectLiteralNode
    (Node source
    ,String suffix
    ,String namespace
    ,URI uri
    ,List<Node> paramNodes
    )
  { 
    this.source=source;
    this.suffix=suffix;
    this.namespace=namespace;
    this.uri=uri;
    this.paramNodes=paramNodes;
  }
  
  @Override
  public Node[] getSources()
  { return new Node[] {source};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    boolean dirty=false;
    List<Node> params=null;
    if (paramNodes!=null)
    {
      params=new ArrayList<Node>();
      for (Node node:paramNodes)
      { 
        Node paramCopy=node.copy(visitor);
        params.add(paramCopy);
        if (node!=paramCopy)
        { dirty=true;
        }
      }
      
    }

    
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
      return new ObjectLiteralNode<Tobject,Treturn>
        (source,suffix,namespace,uri,params);
    }
    else
    { return this;
    }
  } 
  
  @Override
  public String reconstruct()
  { 
    if (namespace!=null)
    { return "[*"+namespace+":"+suffix+"]";
    }
    else if (uri!=null)
    { return "[*:"+uri+"]";
    }
    else
    { return "[*"+suffix+"]";
    }
  }
  
  @SuppressWarnings("unchecked")
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
    
    Reflector<Tobject> reflector=TypeModel.<Tobject>searchType(uri);
    
    
    Focus<?> metaFocus=null;
    if (reflector!=null)
    { metaFocus=focus.chain(reflector.getSelfChannel());
    }
    
    if (metaFocus!=null && metaFocus.getSubject()!=null)
    { 
      // Find out if the reflector for the type supports instantiation
      // We look at the reflector for the reflector for the specified type
      if (metaFocus.getSubject().getReflector().isFunctor())
      {
        
        Expression[] params;
        if (paramNodes!=null)
        { 
          int i=0;
          params=new Expression[paramNodes.size()];
          for (Node paramNode:paramNodes)
          { params[i++]=new Expression(paramNode);
          }
        }
        else
        { params=new Expression<?>[0];
        }
        
        // Use instantiation Functor to get object literal instance
        Channel<Tobject> constructorChannel
          =metaFocus.getSubject()
            .resolve(focus, "" , params);
        
        Tobject object=constructorChannel.get();
        
        Channel sourceChannel=focus.bind(new Expression(source));
        Focus<?> sourceFocus=focus.chain(sourceChannel);
        if (object instanceof FocusChainObject)
        {
          ((FocusChainObject) object).bind(sourceFocus);
        }
        
        Channel<Treturn> returnChannel;
        if (object instanceof ChannelFactory)
        {
          log.fine("Binding ChannelFactory "+object);
          // The object itself creates its own channel from the source
          // By not using sourceFocus here, we give the ChannelFactory
          //   an opportunity to reference objects with the same URI
          //   further up the chain- ie. a "parent" object of the same
          //   type.
          
          returnChannel=((ChannelFactory<Treturn,Tobject>) object)
            .bindChannel(focus.bind(new Expression(source)), focus, null);
        }
        else
        { 
          log.fine("Exposing "+object);
          // Make the object itself available
          Reflector<Tobject> targetReflector=constructorChannel.getReflector();
          if (object instanceof Reflectable<?>)
          { targetReflector=((Reflectable<Tobject>) object).reflect();
          }
          
        // Construct object into a constant channel.
          returnChannel=
            new SimpleChannel<Treturn>
            ((Reflector<Treturn>) targetReflector
            ,(Treturn) object
            ,true
            );
        
        }
            
        
        return returnChannel;
      }
      else
      { 
        throw new BindException
          ("Type '"+uri+"' does not provide access to a constructor");
      }
    }
    else
    { 
      throw new BindException
        ("Type '"+uri+"' not found. "+TypeModel.getRegisteredModels());
    }
  }

  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Functor");
    prefix=prefix+"  ";
    out.append(prefix).append("namespace="+(namespace!=null?namespace:"(default)"));
    if (suffix!=null)
    { out.append(prefix).append("name="+suffix);
    }
  }
  

  
  
}
