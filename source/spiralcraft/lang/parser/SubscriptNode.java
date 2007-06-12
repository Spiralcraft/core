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
import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.optics.LenseBinding;
import spiralcraft.lang.optics.ArrayIndexLense;


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

//  @SuppressWarnings("unchecked") // Upcast for narrowing index type
  public Optic bind(Focus<?> focus)
    throws BindException
  {
    Optic<C> collection=focus.<C>bind(new Expression<C>(_source,null));
    Optic<I> selector=focus.<I>bind(new Expression<I>(_selector,null));
    
    Class clazz=selector.getContentType();
    if (Integer.class.isAssignableFrom(clazz)
        || Short.class.isAssignableFrom(clazz)
        || Byte.class.isAssignableFrom(clazz)
        )
    { 
      if (collection.getContentType().isArray())
      {
        return (Optic<T>) ArrayIndexHelper.<T,C>bind
          (focus,collection, selector);
      }
      else
      { throw new BindException("Unknown Collection Type");
      }
    }
    else
    { throw new BindException("Unknown Selector Type");
    }
  }
  
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Subscript");
    prefix=prefix+"  ";
    _source.dumpTree(out,prefix);
    out.append(prefix).append("[");
    _selector.dumpTree(out,prefix);
    out.append(prefix).append("]");
  }
  
}

class ArrayIndexHelper
{
  
  @SuppressWarnings("unchecked") // Upcasts bc/we narrowed operation type
  public static final <T,C> Optic<T> bind
    (Focus<?> focus
     ,Optic<C> collection
     ,Optic selector
     )
     throws BindException
  {
    return new LenseBinding<T,T[]>
      ((Optic<T[]>) collection
      ,new ArrayIndexLense<T>
        (OpticFactory.getInstance()
            .<T>findPrism((Class<T>) collection.getContentType().getComponentType())
        )
      ,new Optic[] {selector}
      );
    
  }

}