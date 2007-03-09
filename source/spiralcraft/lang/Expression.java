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
package spiralcraft.lang;

import spiralcraft.lang.parser.Node;

import spiralcraft.lang.parser.ExpressionParser;

import java.util.HashMap;

/**
 * <P>An Expression is a series of keywords, constants and operators that
 *   define a dataflow tree. A dataflow tree computes a single value in terms of
 *   a set of operations against the set of data reachable through a Focus.
 *   
 * <P>Internally, an expression is composed of a tree of Nodes. Each node in the
 *   tree represents an operation to be performed on the output of its child nodes.
 *   
 * <P>The textual representation of an Expression is compiled into an Expression
 *   object using the ExpressionParser. The tree of Nodes is the resulting parse tree.
 *   
 * <P>An Expression can also be created programmatically by creating a tree of Nodes.
 * 
 * <P>To create the dataflow Channel, an Expression is bound to a Focus. A Focus
 *    provides access a defined set of Optics (dataflow sources) that the Expression
 *    can refer to in order to realize the dataflow tree. 
 * 
 * <P>The output value of the resulting Channel will continuously reflect the state
 *    of the dataflow sources it is bound to.
 */
public class Expression<T>
{
  private static final HashMap<String,Expression<?>> _CACHE
    =new HashMap<String,Expression<?>>();

  private Node _root;
  private String _text;
  
  @SuppressWarnings("unchecked") // Heterogeneous HashMap
  public static <X> Expression<X> parse(String text)
    throws ParseException
  { 
    synchronized (_CACHE)
    { 
      Expression<X> ret=(Expression<X>) _CACHE.get(text);
      if (ret==null)
      { 
        ret=new ExpressionParser().<X>parse(text);
        _CACHE.put(text,ret);
      }
      return ret;
    }
  }

  public Expression(Node root,String text)
  { 
    _root=root;
    _text=text;
  }

  public String getText()
  { return _text;
  }

  /**
   * @return The root node of the dataflow tree for this expression.
   */
  public Node getRootNode()
  { return _root;
  }
  
  /**
   * Create a Channel by binding this Expression to a Focus. This method
   *   is intended to be used by Focus implementors.
   *
   * Users should use Focus.bind(Expression exp) to permit the Focus to 
   *   re-use Channels defined by the same Expression.
   */
  @SuppressWarnings("unchecked") // Nodes are not generic
  public Channel<T> bind(Focus<?> focus)
    throws BindException
  { 
    if (_root==null)
    { throw new BindException("No way to bind expression '"+_text+"'");
    }
    return new Channel<T>(focus,_root.bind(focus),this); 
  }

  
  public String toString()
  { return super.toString()+"["+_text+"]:"+_root.toString();
  }
  
  public void dumpParseTree(StringBuffer out)
  { _root.dumpTree(out,"\r\n");
  }
}
