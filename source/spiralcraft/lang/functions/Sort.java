//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.lang.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * A ChannelFactory which sorts a Collection 
 * 
 * @author mike
 *
 * @param <Tcollection>
 * @param <Titem>
 * @param <Tfunction>
 */
public class Sort<Tcollection,Titem,Tfunction>
  implements ChannelFactory<Tcollection,Tcollection>
{

  private Ordering<Titem,?> order;
    
  
  public Sort(Ordering<Titem,?> order)
  { this.order=order;
  }
  
  public Sort(boolean reverse)
  { this.order=new Ordering<Titem,Titem>(reverse);
  }
  
  public Sort()
  { this.order=new Ordering<Titem,Titem>();
  }
  
  @Override
  public Channel<Tcollection> bindChannel(
    Channel<Tcollection> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    
    return new SortChannel
      (focus
      ,source
      );
  }
  
  public class SortChannel
    extends SourcedChannel<Tcollection,Tcollection>
    implements Comparator<Titem>
  {

    private final ThreadLocalChannel<Titem> item1Channel;
    private final ThreadLocalChannel<Titem> item2Channel;
    private final CollectionDecorator<Tcollection,Titem> decorator;
    private final Ordering<Titem,?>.OrderingComparator comparator;
    
    @SuppressWarnings("unchecked")
    public SortChannel
      (Focus<?> focus
      ,Channel<Tcollection> source
      )
      throws BindException
    { 
      super(source.getReflector(),source);

      decorator=source.<CollectionDecorator>decorate(CollectionDecorator.class);
      
      this.item1Channel
        =new ThreadLocalChannel<Titem>(decorator.getComponentReflector());
      this.item2Channel
        =new ThreadLocalChannel<Titem>(decorator.getComponentReflector());
      
      comparator
        =order.bind
          (focus.telescope(item1Channel)
          ,focus.telescope(item2Channel)
          );
    }
    
    
    @Override
    protected Tcollection retrieve()
    {
      ArrayList<Titem> list=new ArrayList<Titem>();
      for (Titem i:decorator)
      { list.add(i);
      }
      Collections.sort(list,this);
      
      Tcollection ret=decorator.newCollection();
      ret=decorator.addAll(ret,list.iterator());
      return ret;
      
    }
  
    
    @Override
    protected boolean store(
      Tcollection val)
      throws AccessException
    { return false;
    }


    @Override
    public int compare(
      Titem o1,
      Titem o2)
    {
      item1Channel.push(o1);
      item2Channel.push(o2);
      try
      { return comparator.compare();
      }
      finally
      { 
        item2Channel.pop();
        item1Channel.pop();
      }
    }
    
 
  }

}
