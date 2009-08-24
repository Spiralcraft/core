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
 * Represents an aggregate projection operation.
 * 
 * @author mike
 *
 * @param <T> The projection (return element) type
 * @param <C> The collection type
 */
public class AggregateProjectNode<T,C>
  extends Node
{

  private final Node _source;
  private final Node _projection;

  public AggregateProjectNode(Node source,Node selector)
  { 
    _source=source;
    _projection=selector;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_source,_projection};
  }
    
  @Override
  public Node copy(Object visitor)
  { 
    return new AggregateProjectNode<T,C>
      (_source.copy(visitor),_projection.copy(visitor));
  }
  
  @Override
  public String reconstruct()
  { return _source.reconstruct()+" # [ "+_projection.reconstruct()+" ] ";
  }
  
  @Override
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  {
   
    Channel<C> collection=focus.<C>bind(new Expression<C>(_source,null));
    Channel<?> result=
      collection.resolve
        (focus
        , "#"
        , new Expression[] {new Expression<T>(_projection,null)}
        );
    if (result==null)
    { 
      throw new BindException
        ("Channel could not intepret the # (projection) operator: "
        +collection+" # "+_projection
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
    _projection.dumpTree(out,prefix);
    out.append(prefix).append("]");
  }
  

}