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
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;


/**
 * Represents a contains operation.
 * 
 * @author mike
 *
 * @param <T> The base (content) type
 * @param <C> The collection type
 * @param <I> The index or selector type
 */
public class ContainsNode<T,C,I>
  extends Node
{

  private final Node _source;
  private final Node _compareItem;

  public ContainsNode(Node source,Node selector)
  { 
    _source=source;
    _compareItem=selector;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_source,_compareItem};
  }
    
  @Override
  public Node copy(Object visitor)
  { 
    ContainsNode<T,C,I> copy
      =new ContainsNode<T,C,I>
      (_source.copy(visitor),_compareItem.copy(visitor));
    if (copy._source==_source && copy._compareItem==_compareItem)
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return _source.reconstruct()+" ?= "+_compareItem.reconstruct();
  }
  
  @Override
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  {
   
    Channel<C> collection=focus.<C>bind(Expression.<C>create(_source));
    Channel<?> result=
      collection.resolve
        (focus
        , "?="
        , new Expression[] {Expression.<I>create(_compareItem)}
        );
    if (result==null)
    { 
      throw new BindException
        ("Channel could not intepret the ?= (contains) operator: "
        +collection+" ?= "+_compareItem
        );
    }
    return result;
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Subscript");
    prefix=prefix+"  ";
    _source.dumpTree(out,prefix);
    out.append(prefix).append("[");
    _compareItem.dumpTree(out,prefix);
    out.append(prefix).append("]");
  }
  

}