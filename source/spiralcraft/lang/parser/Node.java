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


import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.log.ClassLog;

import java.net.URI;
import java.util.List;


/**
 * A Node in an Expression parse tree
 */
public abstract class Node
{

  protected final ClassLog log
    =ClassLog.getInstance(getClass());
  
  /**
   * Stubbed bind method for unimplemented nodes.
   *
   * @param focus 
   * @return An optic with no functionality
   * @throws BindException 
   */
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  { 
    unsupported("bind");
    return null;
  }

  public abstract Node copy(Object visitor);
  
  /**
   * Obtain the immediate children of this Node within the syntax tree
   *
   * @return the Nodes that this Node directly depends on
   */
  public abstract Node[] getSources();
  
  /** 
   * <p>Recursively reconstruct the expression text that created this node.
   * </p>
   * 
   * @return The expression text
   */
  public abstract String reconstruct();
  
  public abstract void dumpTree(StringBuffer out,String prefix);

  public void debugTree(java.io.PrintStream err)
  {
    StringBuffer out=new StringBuffer();
    dumpTree(out,"\r\n  ");
    err.println(out.toString());
    
  }
  
  /**
   * <p>Resolves a qName against a prefix resolver and returns the mapped
   *   URI, which resolves the local name against the returned URI
   *   as [mappedUri] / [localName]
   * </p>
   * 
   * <p>A null prefix will resolve the namespace mapped to the
   *   "" empty string
   * </p>
   * 
   * @param prefix
   * @param suffix
   * @param nsr
   * @return
   */
  protected URI resolveQName
    (String prefix,String suffix,PrefixResolver nsr)
  {
    URI uri=null;
    if (nsr!=null)
    {
      if (prefix!=null)
      { uri=nsr.resolvePrefix(prefix);
      }
      else
      { uri=nsr.resolvePrefix("");
      }
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
    return uri;
  }   
  
  /**
   * <p>Resolves a qName against the contextual prefix resolver and returns the
   *   mapped
   *   URI, which resolves the local name against the returned URI
   *   as [mappedUri] / [localName]
   * </p>
   *
   * <p>A null prefix will resolve the namespace mapped to the
   *   "" empty string
   * </p>
   * 
   * <p>Returns null if no contextual prefix resolver is defined
   * </p>
   * 
   * @param prefix
   * @param suffix
   * @return
   */
  protected URI resolveQName
    (String prefix,String suffix)
  {
    PrefixResolver resolver=NamespaceContext.getPrefixResolver();
    if (resolver!=null)
    { return resolveQName(prefix,suffix,resolver);
    }
    else
    { return null;
    }
  }
  
  
  @SuppressWarnings("unchecked") // Genericized for internal purposes only
  public final Node isEqual(Node source)
  { return new EqualityNode(false,this,source);
  }
  
  @SuppressWarnings("unchecked") // Genericized for internal purposes only
  public final Node isNotEqual(Node source)
  { return new EqualityNode(true,this,source);
  }
  
  public final Node call(String identifier,List<Node> params)
  { return new MethodCallNode(this,identifier,params);
  }
  
  @SuppressWarnings("unchecked") // Genericized for internal purposes only
  public final Node resolve(String identifier)
  { return new ResolveNode(this,identifier);
  }

  public Node and(Node op)
  { return new LogicalAndNode(this,op);
  }
  
  public Node or(Node op)
  { return new LogicalOrNode(this,op);
  }
  
  public Node not()
  { return new LogicalNegateNode(this);
  }
  
  public Node xor(Node op)
  { return new ExclusiveOrNode(this,op);
  }
  
  public Node onCondition(Node trueResult,Node falseResult)
  { return new ConditionalNode(this,trueResult,falseResult);
  }

  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node negative()
  { return new NumericNegateNode(this);
  }

  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node plus(Node op)
  { return new NumericOpNode(this,op,'+');
  }
  
  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node minus(Node op)
  { return new NumericOpNode(this,op,'-');
  }
  
  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node times(Node op)
  { return new NumericOpNode(this,op,'*');
  }
  
  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node divide(Node op)
  { return new NumericOpNode(this,op,'/');
  }
  
  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node modulus(Node op)
  { return new NumericOpNode(this,op,'%');
  }

  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node greaterThan(Node op)
  { return new RelationalNode(true,false,this,op);
  }
  
  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node lessThan(Node op)
  { return new RelationalNode(false,false,this,op);
  }

  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node greaterThanOrEquals(Node op)
  { return new RelationalNode(true,true,this,op);
  }
  
  @SuppressWarnings("unchecked") // Generics for internal use only
  public Node lessThanOrEquals(Node op)
  { return new RelationalNode(false,true,this,op);
  }  
  
  @SuppressWarnings("unchecked")
  public Node subscript(Node index)
  { return new SubscriptNode(this,index);
  }
    
  @SuppressWarnings("unchecked")
  public Node assign(Node source)
  { return new AssignmentNode(this,source);
  }

  @SuppressWarnings("unchecked")
  public Node contains(Node source)
  { return new ContainsNode(this,source);
  }

  @SuppressWarnings("unchecked")
  protected Node projectAggregate(Node projection)
  { return new AggregateProjectNode(this,projection);
  }
  
  @SuppressWarnings("unchecked")
  protected Node subcontext(List<Node> subcontextList)
  { return new SubcontextNode(this,subcontextList);
  }
  
  @SuppressWarnings("unchecked")
  protected Node bindFrom(Node source)
  { return new BindingNode(this,source);
  }
  
  protected void unsupported(String msg)
  { throw new UnsupportedOperationException(getClass().getName()+"."+msg+"(...)");
  }
  
  
}
