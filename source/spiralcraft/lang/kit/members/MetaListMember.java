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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class MetaListMember<T,C>
  extends Member<Reflector<T>,List<C>,T>
{


  { name="@list";
  }
  
  @Override
  public Channel<List<C>> resolve(
    Reflector<T> reflector,
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {     
    assertNoArguments(arguments);
    return new IterAsListChannel<T,C>(source);
  }

}

class IterAsListChannel<T,C>
  extends SourcedChannel<T,List<C>>
{
  private final IterationDecorator<T,C> decorator;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public IterAsListChannel(Channel<T> source)
      throws BindException
      {
    super(BeanReflector.<List<C>>getInstance(List.class),source);
    this.decorator
    =source.<IterationDecorator>decorate(IterationDecorator.class);
    if (decorator==null)
    { 
      throw new BindException
      (source.getReflector().getTypeURI()+" does not support @list");
    }
      }

  @Override
  public List<C> retrieve()
  { 
    Iterator<C> iter=decorator.iterator();
    if (iter==null)
    { return null;
    }
    else
    { 
      ArrayList<C> list=new ArrayList<C>();
      while (iter.hasNext())
      { list.add(iter.next());
      }
      return list;
    }
  }

  @Override
  public boolean isWritable()
  { return false;
  }

  @Override
  public boolean store(List<C> val)
  { 
    // XXX Can support this if there's a CollectionDecorator
    return false;
  }   
}