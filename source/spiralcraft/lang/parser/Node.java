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
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.log.ClassLog;

import java.util.List;


/**
 * A Node in an Expression parse tree
 */
public abstract class Node
{

  protected static final ClassLog log
    =ClassLog.getInstance(Node.class);
  
  /**
   * Stubbed bind method for unimplemented nodes.
   *
   *@return An optic with no functionality
   */
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  { 
    unsupported("bind");
    return null;
  }

  public abstract Node copy(Object visitor);
  
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

  protected void unsupported(String msg)
  { throw new UnsupportedOperationException(getClass().getName()+"."+msg+"(...)");
  }
}
