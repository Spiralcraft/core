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
package spiralcraft.lang.spi;


import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

import spiralcraft.util.ArrayUtil;

import java.util.Iterator;

/**
 * Implements an IterationDecorator for a source that returns an Array
 */
public class ArrayIterationDecorator<I>
  extends IterationDecorator<I[],I>
{
  public ArrayIterationDecorator(Channel<I[]> source,Reflector<I> componentReflector)
  { super(source,componentReflector);
  }
  
  public Iterator<I> createIterator()
  { 
    I[] array=source.get();
    if (array!=null)
    { return ArrayUtil.<I>iterator( array);
    }
    else
    { return null;
    }
  }

}
