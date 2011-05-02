//
// Copyright (c) 1998,2011 Michael Toth
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
package spiralcraft.lang.kit.members;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.kit.Member;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * Returns 
 */
public class MetaSizeMember<T>
  extends Member<Reflector<T>,Integer,T>
{


  { name="@size";
  }
  
  @Override
  public Channel<Integer> resolve(
    Reflector<T> reflector,
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  { 
    assertNoArguments(arguments);
    return new CollectionSizeChannel<T>(source);
  }

}

class CollectionSizeChannel<T>
  extends SourcedChannel<T,Integer>
{
  private final CollectionDecorator<T,?> decorator;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public CollectionSizeChannel(Channel<T> source)
    throws BindException
  {
    super(BeanReflector.<Integer>getInstance(Integer.class),source);
    this.decorator
      =source.<CollectionDecorator>decorate(CollectionDecorator.class);
    if (decorator==null)
    { 
      throw new BindException
        (source.getReflector().getTypeURI()+" does not support @size()");
    }
  }

  @Override
  public Integer retrieve()
  { 
    T collection=source.get();
    if (collection==null)
    { return null;
    }
    else
    { return decorator.size(collection);
    }
  }

  @Override
  public boolean store(Integer val)
  { return false;
  }  
}