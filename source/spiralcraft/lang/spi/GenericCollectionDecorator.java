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


import spiralcraft.lang.AccessException;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

import java.util.Collection;
import java.util.Iterator;

/**
 * Implements an CollectionDecorator for a source that returns a
 *   Collection type
 */
public class GenericCollectionDecorator<C extends Collection<I>,I>
  extends CollectionDecorator<C,I>
{
  public GenericCollectionDecorator
    (Channel<C> source
    ,Reflector<I> componentReflector
    )
  { super(source,componentReflector);
  }
  
  @Override
  public Iterator<I> createIterator()
  { return source.get().iterator();
  }

  @Override
  public void add(
    C collection,
    I item)
  { collection.add(item);
  }

  @Override
  public C newCollection()
  { 
    try
    { return source.getReflector().getContentType().newInstance();
    }
    catch (IllegalAccessException x)
    { 
      throw new AccessException
        ("Error creating collection: "+source.getReflector(),x);
    }
    catch (InstantiationException x)
    {
      throw new AccessException
        ("Error creating collection: "+source.getReflector(),x);
    }
  }

  
}
