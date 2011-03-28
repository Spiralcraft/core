//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.lang.spi;


import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ListDecorator;
import spiralcraft.lang.Reflector;

import spiralcraft.util.ArrayUtil;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Implements an IterationDecorator for a source that returns an Array
 */
public class ArrayListDecorator<I>
  extends ListDecorator<I[],I>
{
  public ArrayListDecorator(Channel<I[]> source,Reflector<I> componentReflector)
    throws BindException
  { 
    super(source,componentReflector);
    if (!source.getReflector().getContentType().isArray())
    { 
      throw new BindException
        ("ArrayListDecorator source type is not compatible with Object[]: "
        +source.getReflector().getContentType()
        );
    }
  }
  
  @Override
  public Iterator<I> createIterator()
  { 
    try
    {
      I[] array=source.get();
      if (array!=null)
      { return ArrayUtil.<I>iterator( array);
      }
      else
      { return null;
      }
    }
    catch (ClassCastException x)
    { 
      throw new AccessException
        ("Incompatible type returned by source: "+source ,x);
    }
  }

  @Override
  public I[] add(
    I[] collection,
    I item)
  { return ArrayUtil.append(collection,item);
  }
  
  @Override
  public I[] addAll(
    I[] collection
    ,Iterator<I> items
    )
  {
    LinkedList<I> itemList=new LinkedList<I>();
    while (items.hasNext())
    { itemList.add(items.next());
    }
    return ArrayUtil.concat(collection,itemList);
    
  }

  @Override
  public I[] addAll(
    I[] collection
    ,I[] items
    )
  {
    return ArrayUtil.concat(collection,items);
    
  }  
  
  @SuppressWarnings("unchecked")
  @Override
  public I[] newCollection()
  { 
    return (I[]) Array.newInstance
      (getComponentReflector().getContentType(),0);
  }
  
  @Override
  public int size(I[] collection)
  { return collection.length;
  }
  
  @Override
  public I get(I[] collection,int index)
  { 
    if (index<collection.length)
    { return collection[index];
    }
    else
    { return null;
    }
  }

}
