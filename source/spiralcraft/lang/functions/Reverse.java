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

import java.util.LinkedList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SourcedChannel;

/**
 * A ChannelFactory which reverses a Collection 
 * 
 * @author mike
 *
 * @param <Tcollection>
 * @param <Titem>
 * @param <Tfunction>
 */
public class Reverse<Tcollection,Titem,Tfunction>
  implements ChannelFactory<Tcollection,Tcollection>
{

    
  
  public Reverse()
  { 
  }
  
  
  @Override
  public Channel<Tcollection> bindChannel(
    Channel<Tcollection> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    
    return new ReverseChannel
      (focus
      ,source
      );
  }
  
  public class ReverseChannel
    extends SourcedChannel<Tcollection,Tcollection>
  {

    private final CollectionDecorator<Tcollection,Titem> decorator;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ReverseChannel
      (Focus<?> focus
      ,Channel<Tcollection> source
      )
      throws BindException
    { 
      super(source.getReflector(),source);

      decorator=source.<CollectionDecorator>decorate(CollectionDecorator.class);
      if (decorator==null)
      { throw new BindException("Not a collection "+source.getReflector());
      }

    }
    
    
    @Override
    protected Tcollection retrieve()
    {
      LinkedList<Titem> list=new LinkedList<Titem>();
      for (Titem i:decorator)
      { list.addFirst(i);
      }
      
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
  }

}
