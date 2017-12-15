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

import java.util.Iterator;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.kit.Member;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * Returns 
 */
public class MetaEmptyMember<T>
  extends Member<Reflector<T>,Boolean,T>
{


  { name="@empty";
  }
  
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Channel<Boolean> resolve(
    Reflector<T> reflector,
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  { 
    assertNoArguments(arguments);
    if (source.<IterationDecorator>decorate(IterationDecorator.class)!=null)
    { return new CollectionEmptyChannel<T>(source);
    }
    else if (source.getReflector().getContentType()==String.class)
    { return new StringEmptyChannel((Channel<String>) source);
    }
    else
    {
      throw new BindException
        (source.getReflector().getTypeURI()+" does not support @empty");
    }
  }

}

class CollectionEmptyChannel<T>
  extends SourcedChannel<T,Boolean>
{
  private final IterationDecorator<T,?> decorator;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public CollectionEmptyChannel(Channel<T> source)
      throws BindException
  {
    super(BeanReflector.<Boolean>getInstance(Boolean.class),source);
    this.decorator
      =source.<IterationDecorator>decorate(IterationDecorator.class);
    if (decorator==null)
    { 
      throw new BindException
        (source.getReflector().getTypeURI()+" does not support @empty");
    }
  }

  @Override
  public Boolean retrieve()
  { 
    Iterator<?> iter=decorator.iterator();
    if (iter==null)
    { return null;
    }
    else
    { return !iter.hasNext();
    }
  }

  @Override
  public boolean store(Boolean val)
  { return false;
  }   
}

class StringEmptyChannel
  extends SourcedChannel<String,Boolean>
{

  public StringEmptyChannel(Channel<String> source)
    throws BindException
  {
    super(BeanReflector.<Boolean>getInstance(Boolean.class),source);
  }

  @Override
  public Boolean retrieve()
  { 
    String val=source.get();
    if (val==null || val.isEmpty())
    { return true;
    }
    return false;
  }

  @Override
  public boolean store(Boolean val)
  { return false;
}   
}