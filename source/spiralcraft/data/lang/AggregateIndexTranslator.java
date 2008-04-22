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
package spiralcraft.data.lang;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;

import spiralcraft.lang.spi.Translator;

public class AggregateIndexTranslator<T>
  implements Translator<T,Aggregate<T>>
{
  private final Reflector<T> contentReflector;
  
  @SuppressWarnings("unchecked")
  public AggregateIndexTranslator(DataReflector<Aggregate<T>> aggregateReflector)
    throws BindException
  { 
    this.contentReflector
      =DataReflector.getInstance
           (aggregateReflector.getType().getContentType());
  }
  
  public Reflector<T> getReflector()
  { return contentReflector;
  }

  @SuppressWarnings("unchecked") // Upcast for expected modifiers
  @Override
  public T translateForGet(Aggregate<T> source,Channel<?>[] modifiers)
  { 
    if (modifiers==null || modifiers.length==0)
    { return null;
    }
    Number index=((Channel<Number>) modifiers[0]).get();
    if (index==null)
    { return null;
    }
    if (source==null)
    { return null;
    }
    try
    { return source.get(index.intValue());
    }
    catch (DataException x)
    { 
      throw new AccessException
        ("Error retrieving index "+index.intValue()+" in "+source.getType());
    }
  }

  @SuppressWarnings("unchecked") // Upcast for expected modifiers
  public Aggregate<T> translateForSet(T value,Channel[] modifiers)
  { throw new UnsupportedOperationException("Can't reverse array index");
  }


}
