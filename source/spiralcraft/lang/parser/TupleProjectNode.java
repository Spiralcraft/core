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
 * Represents a tuple projection operation.
 * 
 * @author mike
 *
 * @param <T> The projection (return element) type
 * @param <S> The source type
 */
public class TupleProjectNode<T,S>
  extends Node
{

  private final Node _source;
  private final Node _tuple;

  public TupleProjectNode(Node source,Node tuple)
  { 
    _source=source;
    _tuple=tuple;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_source,_tuple};
  }
    
  @Override
  public Node copy(Object visitor)
  { 
    return new TupleProjectNode<T,S>
      (_source.copy(visitor),_tuple.copy(visitor));
  }
  
  @Override
  public String reconstruct()
  { return _source.reconstruct()+_tuple.reconstruct();
  }
  
  @Override
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  {
   
    Channel<S> sourceChannel=focus.<S>bind(new Expression<S>(_source,null));
    Channel<?> result
      =focus.telescope(sourceChannel).bind(new Expression<T>(_tuple,null));
    return result;
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Tuple projection ");
    prefix=prefix+"  ";
    _source.dumpTree(out,prefix);
    _tuple.dumpTree(out,prefix);
  }
  

}