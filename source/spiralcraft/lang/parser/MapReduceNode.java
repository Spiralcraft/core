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
import spiralcraft.lang.spi.IterationProjector;


/**
 * Represents an Map or Reduce operation against an iterable
 * 
 * @author mike
 *
 * @param <T> The projection (return element) type
 * @param <C> The collection type
 */
public class MapReduceNode<T,C>
  extends Node
{

  private final Node _source;
  private final Node _function;
  private final boolean _reduce;

  public MapReduceNode(Node source,Node function,boolean reduce)
  { 
    _source=source;
    _function=function;
    _reduce=reduce;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_source,_function};
  }
    
  @Override
  public Node copy(Object visitor)
  { 
    MapReduceNode<T,C> copy
      =new MapReduceNode<T,C>
        (_source.copy(visitor),_function.copy(visitor),_reduce);
    if (copy._source==_source && copy._function==_function)
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return _source.reconstruct()
           +(!_reduce
            ?" # { "+_function.reconstruct()+" } "
            :" $ [ "+_function.reconstruct()+" ] " 
            );
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  {
   
    Channel<C> collection=focus.<C>bind(new Expression<C>(_source,null));
    Expression<T> function=new Expression<T>(_function);

    return new IterationProjector(collection,focus,function,_reduce).result;
    
//    Channel<?> result=
//      collection.resolve
//        (focus
//        , !_reduce?"#":"$"
//        , new Expression[] {function}
//        );
//    
//    // Default behavior
//    if (result==null 
//        && collection
//            .<IterationDecorator>decorate(IterationDecorator.class)!=null
//       )
//    { 
//      return new IterationProjector
//        (collection,focus,function,_reduce).result;
//    }
//    
//    if (result==null)
//    {
//      throw new BindException
//        ("Channel could not intepret the "
//          +(!_reduce?"# (map)":"$ (reduce)")
//          +" operator, and does not"
//          +"support iteration: "
//          +collection+(!_reduce?" # ":" $ ")+_function
//        );
//    }
//    return result;
    
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    if (!_reduce)
    {
      out.append(prefix).append("Map");
      prefix=prefix+"  ";
      _source.dumpTree(out,prefix);
      out.append(prefix).append("{");
      _function.dumpTree(out,prefix);
      out.append(prefix).append("}");
    }
    else
    {
      out.append(prefix).append("Reduce");
      prefix=prefix+"  ";
      _source.dumpTree(out,prefix);
      out.append(prefix).append("[");
      _function.dumpTree(out,prefix);
      out.append(prefix).append("]");
    }
  }
  

}