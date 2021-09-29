//
// Copyright (c) 2021 Michael Toth
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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;

/**
 * Provides a spiralcraft.lang.Binding object that reference an expression to be 
 *   used for deferred binding. 
 *   
 * @author mike
 *
 */
public class BindingLiteralNode
  extends Node
{

  private final Node node;
  private final Expression<?> expression;
  
  @SuppressWarnings({ "rawtypes", "unchecked"})
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  { 
    // Always create a new Binding due to bind-context sensitivity
    return LangUtil.constantChannel(new Binding(expression));
  }

  public BindingLiteralNode(Node node)
  { 
    this.node=node;
    this.expression=Expression.create(node);
  }

  @Override
  public Node copy(Object visitor)
  { return new BindingLiteralNode(node.copy(visitor));
  }

  @Override
  public Node[] getSources()
  { return new Node[] {node};
  }

  @Override
  public String reconstruct()
  { return ":"+node.reconstruct();
  }

  @Override
  public void dumpTree(
    StringBuffer out,
    String prefix)
  {
    out.append(prefix+": ");
    node.dumpTree(out, prefix+" ");
    
    
  }

}