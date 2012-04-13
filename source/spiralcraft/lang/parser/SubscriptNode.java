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
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;

//import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ArrayConstructorChannel;
//import spiralcraft.lang.spi.ArrayIndexTranslator;
//import spiralcraft.lang.spi.TranslatorChannel;
import spiralcraft.lang.spi.PrimitiveArrayConstructorChannel;


/**
 * Represents a subscript operation.
 * 
 * @author mike
 *
 * @param <T> The base (content) type
 * @param <C> The collection type
 * @param <I> The index or selector type
 */
public class SubscriptNode<T,C,I>
  extends Node
{

  private final Node _source;
  private final Node _selector;

  public SubscriptNode(Node source,Node selector)
  { 
    _source=source;
    _selector=selector;
  }

  @Override
  public Node[] getSources()
  { return new Node[] {_source,_selector};
  }
    
  @Override
  public Node copy(Object visitor)
  { 
    SubscriptNode<T,C,I> copy
      =new SubscriptNode<T,C,I>
      (_source.copy(visitor),_selector.copy(visitor));
    if (copy._source==_source && copy._selector==_selector)
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return (_source!=null?_source.reconstruct():"")+"["+_selector.reconstruct()+"]";
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  {
    if (_source instanceof TypeFocusNode)
    { 
      Reflector<T> type=(Reflector<T>) _source.bind(focus).get();
      
      Channel<?> iterChannel=focus.bind(Expression.create(_selector));
      
      
      
      IterationDecorator<?,?> decorator
        =iterChannel.<IterationDecorator>decorate(IterationDecorator.class);
      
      if (decorator.getComponentReflector().getContentType().isPrimitive())
      { 
        return new PrimitiveArrayConstructorChannel
          (type
          ,iterChannel
          ,decorator
          );
           
        
      }
      else
      {
        // Array construction from something that can be iterated
        return new ArrayConstructorChannel
          (type
          ,iterChannel
          ,decorator
          );
      }
    }
    else if (_source==null)
    { 
      // Dynamic array literal
      Channel<?> iterChannel=focus.bind(Expression.create(_selector));
      IterationDecorator<?,?> decorator
        =iterChannel.<IterationDecorator>decorate(IterationDecorator.class);
      Reflector<T> type=(Reflector<T>) decorator.getComponentReflector();
      return new ArrayConstructorChannel(type,iterChannel,decorator);
    }
    else
    {
    
      Channel<C> collection=focus.<C>bind(Expression.<C>create(_source));
      Channel<?> result=
        collection.resolve
          (focus
          , "[]"
          , new Expression[] {Expression.<I>create(_selector,null)}
          );
      
      if (result==null)
      { 
        throw new BindException
          ("Channel could not intepret the [] operator: "
          +collection+"["+_selector+"]"
          );
      }
      return result;
    }
  }
  
//  private Channel<?> bindDefault(Focus<?> focus)
//    throws BindException
//  {
//    
//    Channel<C> collection=focus.<C>bind(new Expression<C>(_source,null));
//    Channel<I> selector=focus.<I>bind(new Expression<I>(_selector,null));
//    
//    Class<?> clazz=selector.getContentType();
//    if (Integer.class.isAssignableFrom(clazz)
//        || Short.class.isAssignableFrom(clazz)
//        || Byte.class.isAssignableFrom(clazz)
//        )
//    { 
//      // XXX Use an IndexDecorator interface 
//      if (collection.getContentType().isArray())
//      {
//        return (Channel<T>) ArrayIndexHelper.<T,C>bind
//          (focus,collection, selector);
//      }
//      else
//      { throw new BindException("Unknown Collection Type");
//      }
//    }
//    else if (Boolean.class.isAssignableFrom(clazz))
//    {
//      // XXX Use SelectionDecorator interface
//      throw new BindException("Unimplemented Selector Type");
//      
//    }
//    else
//    { 
//      // XXX Use MapDecorator interface
//      throw new BindException("Unknown Selector Type");
//    }
//  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Subscript");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }
    out.append(prefix).append("[");
    _selector.dumpTree(out,prefix);
    out.append(prefix).append("]");
  }
  
}

//class ArrayIndexHelper
//{
//  
//  @SuppressWarnings("unchecked") // Upcasts bc/we narrowed operation type
//  public static final <T,C> Channel<T> bind
//    (Channel<C> collection
//     ,Channel selector
//     )
//  {
//    return new TranslatorChannel<T,T[]>
//      ((Channel<T[]>) collection
//      ,new ArrayIndexTranslator<T>
//        (BeanReflector.<T>getInstance
//            ((Class<T>) collection.getContentType().getComponentType())
//        )
//      ,new Channel[] {selector}
//      );
//    
//  }
//
//}