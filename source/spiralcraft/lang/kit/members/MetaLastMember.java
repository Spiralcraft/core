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
import spiralcraft.lang.ListDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.kit.Member;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * Returns 
 */
public class MetaLastMember<T,I>
  extends Member<Reflector<T>,I,T>
{


  { name="@last";
  }
  
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Channel<I> resolve(
    Reflector<T> reflector,
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  { 
    assertNoArguments(arguments);
    Channel<I> channel=source.getCached(name);
    if (channel==null)
    { 
      ListDecorator<T,I> d
        =source.<ListDecorator>decorate(ListDecorator.class);
      if (d!=null)
      { 
        channel=new LastListChannel(source,d);
        source.cache(name,channel);
      }
    }
    if (channel==null)
    { 
      IterationDecorator d
        =source.<IterationDecorator>decorate(IterationDecorator.class);
      if (d!=null)
      { 
        channel=new LastIterChannel(source,d);
        source.cache("@top",channel);
      }
    }
    return channel;
  }

}

class LastListChannel<T,I>
  extends SourcedChannel<T,I>
{
  private final ListDecorator<T,I> decorator;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public LastListChannel(Channel<T> source,ListDecorator decorator)
      throws BindException
      { 
    super
    (decorator.getComponentReflector()
      ,source
        );
    this.decorator=decorator;
      }

  @Override
  public I retrieve()
  { 
    T list=source.get();
    int size=list==null?0:decorator.size(list);
    if (size>0)
    { return decorator.get(list,decorator.size(list)-1);
    }
    else
    { return null;
    }
  }

  @Override
  public boolean store(I val)
  { return false;
  }
}

class LastIterChannel<T,I>
  extends SourcedChannel<T,I>
{
  private final IterationDecorator<T,I> decorator;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public LastIterChannel(Channel<T> source,IterationDecorator decorator)
      throws BindException
      { 
    super
    (decorator.getComponentReflector()
      ,source
        );
    this.decorator=decorator;
      }

  @Override
  public I retrieve()
  { 
    Iterator<I> it=decorator.iterator();
    if (it!=null)
    {
      I next=null;
      while (it.hasNext())
      { next=it.next();
      }
      return next;
    }
    else
    { return null;
    }
  }

  @Override
  public boolean store(I val)
  { return false;
  }
}