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

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;

import spiralcraft.lang.util.LangUtil;

public class ExpressionLiteralNode<T>
  extends Node
{

  private final Node expression;
  private final Channel<Expression<T>> _optic;

  public ExpressionLiteralNode(Node expression)
  { 
    this.expression=expression;
    _optic=LangUtil.constantChannel(Expression.<T>create(expression));
  }
  
  @Override
  public Node[] getSources()
  { return new Node[] {expression};
  }
  
  @Override
  public Node copy(Object visitor)
  { 
    Node copy=expression.copy(visitor);
    if (copy!=expression)
    { return new ExpressionLiteralNode<T>(expression);
    }
    
    return this;
  }
  
  @Override
  public String reconstruct()
  { return "`"+expression.reconstruct()+"`";
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix)
      .append("ExpressionLiteral: ");
    expression.dumpTree(out, prefix+"  ");
    ;
  }

  @Override
  public synchronized Channel<Expression<T>> bind(final Focus<?> focus)
    throws BindException
  { return _optic;
  }
 
  
}
