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
package spiralcraft.lang.optics;


import spiralcraft.lang.BindException;
import spiralcraft.lang.Optic;
import spiralcraft.lang.decorators.IterationDecorator;
import spiralcraft.lang.OpticFactory;

import spiralcraft.util.ArrayUtil;

import java.util.Iterator;

/**
 * Implements an IterationDecorator for a source that returns an Array
 */
public class ArrayIterator<T,I>
  extends AbstractBinding<I>
  implements IterationDecorator<T,I>
{
  
  private Optic<T> source;
  private Iterator<I> iterator;
  private I value;
  
  
  @SuppressWarnings("unchecked") // Array related reflection cast
  public ArrayIterator(Optic<T> source)
    throws BindException
  { 
    super(OpticFactory.getInstance().<I>findPrism
           ((Class<I>) source.getContentType().getComponentType())
         );
    this.source=source;
  }
  
  
  public boolean hasNext()
  { 
    if (iterator==null)
    { return false;
    }
    return iterator.hasNext();
  }

  public void next()
  { 
    if (iterator==null || !iterator.hasNext())
    { value=null;
    }
    else
    { value=iterator.next();
    }
  }

  @SuppressWarnings("unchecked") // Array cast
  public void reset()
  { 
    T val=source.get();
    iterator=ArrayUtil.<I>iterator((I[]) val);
    value=null;
  }

  public boolean store(I val)
  { return false;
  }
  
  public I retrieve()
  { return value;
  }

}
