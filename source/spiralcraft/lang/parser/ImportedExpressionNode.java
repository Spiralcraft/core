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
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

import spiralcraft.lang.BindException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import spiralcraft.util.refpool.URIPool;

/**
 * Parse and bind an expression from the indicated resource.
 * 
 * @author mike
 *
 */
public class ImportedExpressionNode<Tobject,Treturn>
  extends Node
{
  
  private final String suffix;
  private final String namespace;
  private URI uri;
  private Node source;
  private List<Node> paramNodes;

  public ImportedExpressionNode
    (Node source
    ,String qname
    ,List<Node> paramNodes
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
    this.paramNodes=paramNodes;

  }

  ImportedExpressionNode
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
      return new ImportedExpressionNode<Tobject,Treturn>
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
    { return "[~"+namespace+":"+suffix+"]";
    }
    else if (uri!=null)
    { return "[~:"+uri+"]";
    }
    else
    { return "[~"+suffix+"]";
    }
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Channel<Treturn> bind(Focus<?> focus)
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
    
    try
    {
      ExpressionReader expReader = ExpressionReader.read(uri);

      StructNode contextStruct=expReader.getContextStruct();

      Expression[] params;
      if (paramNodes!=null)
      { 
        if (contextStruct==null)
        { throw new BindException("Parameters not supported because no context declared");
        }
        
        int i=0;
        params=new Expression[paramNodes.size()];
        for (Node paramNode:paramNodes)
        { 
          if (!(paramNode instanceof BindingNode))
          { 
            throw new BindException
              ("Parameter for imported expression must be a binding"
               +" in the form <name>:=<value>. ("+paramNode.reconstruct()+")"
              );
          }
          params[i++]=Expression.create(paramNode);
          BindingNode paramBinding = (BindingNode) paramNode;
          ContextIdentifierNode identifierNode 
            = (ContextIdentifierNode) paramBinding.getTarget();
          String name = identifierNode.getIdentifier();
          StructMember member=contextStruct.getMember(name);
          if (member==null)
          { throw new BindException("Unrecognized context param '"+name+"'");
          }
          member.passThrough=true;
          member.source=paramBinding.getSource();
        }
      }
      else
      { params=new Expression<?>[0];
      }
      
      Channel sourceChannel=focus.bind(Expression.create(source));
      if (contextStruct!=null)
      { 
        Channel contextChannel=focus.bind(Expression.create(contextStruct));
        focus=focus.chain(contextChannel);
      }
      
      Focus<?> sourceFocus=focus.telescope(sourceChannel);
      
      Expression expr=expReader.getExpression();
      if (expr!=null)
      { return sourceFocus.bind(expr);
      }
      else
      { throw new BindException("No expression read from "+uri);
      }
    }
    catch (Exception x)
    { throw new BindException("Error importing "+uri,x);
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
