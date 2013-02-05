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
import spiralcraft.common.namespace.UnresolvedPrefixException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

import java.net.URI;
import java.util.List;


/**
 * A Node in an Expression parse tree
 */
public abstract class Node
{

  protected final ClassLog log
    =ClassLog.getInstance(getClass());
  
  protected int hashCode=super.hashCode();
  
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
  
  protected boolean equalsNode(Node node)
  { return node==this;
  }
  
  @Override
  public int hashCode()
  { return hashCode;
  }
  
  @Override
  public boolean equals(Object o)
  { 
    if (o==null)
    { return false;
    }
    if (o==this)
    { return true;
    }
    if (o.getClass()!=getClass())
    { return false;
    }
    if (hashCode!=o.hashCode())
    { return false;
    }
    
    return equalsNode((Node) o);
  }
  
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


      if (uri!=null)
      { 
        String uriStr=uri.toString();
        if (!uriStr.endsWith("/"))
        { 
          uriStr=uriStr+"/";
        }
        // Don't use URI.resolve() here because scheme-nested URIs get mangled
        uriStr=uriStr+suffix;
        uri=URI.create(uriStr);
      }
      else if (suffix!=null 
              && (prefix==null || prefix.isEmpty()) 
              )
      {
        uri=URI.create(suffix);
      }
    
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
    throws UnresolvedPrefixException
  {
    PrefixResolver resolver=NamespaceContext.getPrefixResolver();
    URI ret=null;
    if (resolver!=null)
    { 
      ret=resolveQName(prefix,suffix,resolver);
      if (ret==null)
      {  throw new UnresolvedPrefixException(prefix,suffix,resolver);

      }
    }
    else
    {
      log.log
        (Level.WARNING
        ,"Unresolved qname '"
        +(prefix==null || prefix.isEmpty()?"":prefix+":")+suffix+"'"
        +": No contextual resolver found"
        ,new Exception()
        );
    }
    return ret;
  }
  
  
  @SuppressWarnings("rawtypes")
  public final Node isEqual(Node source)
  { return new EqualityNode(false,this,source);
  }
  
  @SuppressWarnings("rawtypes")
  public final Node isNotEqual(Node source)
  { return new EqualityNode(true,this,source);
  }
  
  public final Node call(String identifier,List<Node> params)
  { return new MethodCallNode(this,identifier,params);
  }
  
  @SuppressWarnings("rawtypes")
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

  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node negative()
  { return new NumericNegateNode(this);
  }

  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node plus(Node op)
  { return new BinaryOpNode(this,op,'+');
  }
  
  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node minus(Node op)
  { return new BinaryOpNode(this,op,'-');
  }
  
  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node times(Node op)
  { return new BinaryOpNode(this,op,'*');
  }
  
  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node divide(Node op)
  { return new BinaryOpNode(this,op,'/');
  }
  
  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node modulus(Node op)
  { return new BinaryOpNode(this,op,'%');
  }

  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node greaterThan(Node op)
  { return new RelationalNode(true,false,this,op);
  }
  
  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node lessThan(Node op)
  { return new RelationalNode(false,false,this,op);
  }

  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node greaterThanOrEquals(Node op)
  { return new RelationalNode(true,true,this,op);
  }
  
  @SuppressWarnings("rawtypes") // Generics for internal use only
  public Node lessThanOrEquals(Node op)
  { return new RelationalNode(false,true,this,op);
  }  
  
  @SuppressWarnings("rawtypes")
  public Node subscript(Node index)
  { return new SubscriptNode(this,index);
  }
    
  @SuppressWarnings("rawtypes")
  public Node assign(Node source)
  { return new AssignmentNode(this,source);
  }

  @SuppressWarnings("rawtypes")
  public Node assignAdditive(Node source)
  { return new AssignmentNode(this,source,'+');
  }

  @SuppressWarnings("rawtypes")
  public Node assignSubtractive(Node source)
  { return new AssignmentNode(this,source,'-');
  }

  @SuppressWarnings("rawtypes")
  public Node assignCoercive(Node source)
  { return new AssignmentNode(this,source,'$');
  }

  @SuppressWarnings("rawtypes")
  public Node contains(Node source)
  { return new ContainsNode(this,source);
  }

  @SuppressWarnings("rawtypes")
  protected Node map(Node projection)
  { return new MapReduceNode(this,projection,false);
  }

  @SuppressWarnings("rawtypes")
  protected Node reduce(Node reduction)
  { return new MapReduceNode(this,reduction,true);
  }
    
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected Node subcontext(List<Node> subcontextList)
  { return new SubcontextNode(this,subcontextList);
  }
  
  @SuppressWarnings("rawtypes")
  protected Node bindFrom(Node source)
  { return new BindingNode(this,source);
  }
  
  protected void unsupported(String msg)
  { throw new UnsupportedOperationException(getClass().getName()+"."+msg+"(...)");
  }
  
  
}
