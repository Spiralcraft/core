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

import spiralcraft.util.EnumerationIterator;

import java.util.Iterator;
import java.util.Enumeration;

/**
 * Implements an IterationDecorator for a source that returns an Array
 */
public class EnumerationIterationDecorator<I>
  extends IterationDecorator<Enumeration<I>,I>
{
  public EnumerationIterationDecorator
    (Channel<Enumeration<I>> source,Reflector<I> componentReflector)
  { super(source,componentReflector);
  }
  
  @Override
  public Iterator<I> createIterator()
  { 
    Enumeration<I> enumeration=source.get();
    if (enumeration!=null)
    { return new EnumerationIterator<I>(enumeration);
    }
    else
    { return null;
    }
  }

}
