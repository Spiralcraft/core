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

import spiralcraft.lang.spi.SimpleChannel;

public class ExpressionLiteralNode
  extends Node
{

  private final Node expression;
  private final SimpleChannel<Expression<?>> _optic;

  @SuppressWarnings("unchecked") // Type check
  public ExpressionLiteralNode(Node expression)
  { 
    this.expression=expression;
    _optic=new SimpleChannel<Expression<?>>(new Expression(expression),true);
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
    { return new ExpressionLiteralNode(expression);
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
  public synchronized Channel<Expression<?>> bind(final Focus<?> focus)
    throws BindException
  { return _optic;
  }
 
  
}
