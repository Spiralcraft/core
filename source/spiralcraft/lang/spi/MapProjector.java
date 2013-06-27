//
// Copyright (c) 1998,2010 Michael Toth
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

import java.lang.reflect.Array;
import java.util.ArrayList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationCursor;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.log.ClassLog;
import spiralcraft.util.lang.ClassUtil;

/**
 * Utility to build a projection Channel on an Iterable
 * 
 * @author mike
 *
 * @param <I>
 * @param <P>
 */
public class MapProjector<I,P,R,C>
{
  @SuppressWarnings("unused")
  private static final ClassLog log
    =ClassLog.getInstance(MapProjector.class);

  public final Channel<R> result;
  
  protected final IterationDecorator<C,I> iterable;
    
  protected final ThreadLocalChannel<I> channel;
  
  @SuppressWarnings("rawtypes")
  protected final ThreadLocalChannel<IterationCursor> cursorChannel;
        
  protected final Focus<I> telefocus;
    
  protected final Channel<P> functionChannel;  
  protected final ViewCache viewCache;
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public MapProjector
    (Channel<C> source
    ,Focus<?> focus
    ,Expression<P> function
    )
    throws BindException
  {
    viewCache=new ViewCache(focus);
    
    iterable
      =source.getReflector()
        .<IterationDecorator>decorate(source,IterationDecorator.class);
    if (iterable==null)
    { throw new BindException("Source is not iterable: "+source.getReflector());
    }
    
    
    
    channel
      =new ThreadLocalChannel<I>(iterable.getComponentReflector());
        
    telefocus=focus.telescope(channel);
    
    cursorChannel
      =new ThreadLocalChannel<IterationCursor>
        (BeanReflector.<IterationCursor>getInstance(IterationCursor.class));
    
    telefocus.addFacet(new SimpleFocus(cursorChannel));
    telefocus.addFacet(viewCache.bind(telefocus));

    
    functionChannel=telefocus.bind(function);
    
    result=new MapChannel
      (aggregateReflector(functionChannel));
  }
  

  
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected  R createArray(ArrayList<P> output)
  {
    Class pclass=functionChannel.getContentType();
    if (pclass.isPrimitive())
    { pclass=ClassUtil.boxedEquivalent(pclass);
    }
    return copyToArray((Object[]) Array.newInstance(pclass,output.size()),output);
    
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private R copyToArray(Object[] array,ArrayList<P> output)
  { 
    int i=0;
    for (P item : output)
    { 
      try
      { array[i++]=item;
      }
      catch (ArrayStoreException x)
      { 
        throw new AccessException
          ("Error adding "+item+" to array of "
            +functionChannel.getReflector().getTypeURI()
          ,x
          );
      }
    }
    return (R) array;
  }
  
  @SuppressWarnings("unchecked")
  protected Reflector<R> aggregateReflector(Channel<P> projection)
    throws BindException
  { 
    return (Reflector<R>) 
      ArrayReflector.getInstance(projection.getReflector());
  }
  
    
  class MapChannel
    extends AbstractChannel<R>
  {
    public MapChannel(Reflector<R> resultReflector)
    { super(resultReflector);
    }

    @Override
    protected R retrieve()
    {
      ArrayList<P> output=new ArrayList<P>();

      IterationCursor<I> it=iterable.iterator();
      cursorChannel.push(it);
      channel.push(null);
      viewCache.push();
      viewCache.init();
      try
      {
        while (it.hasNext())
        {
          I item=it.next();
          channel.set(item);
          viewCache.touch();
          output.add(functionChannel.get());
        }
        viewCache.checkpoint();
        return createArray(output); 
      }
      finally
      { 
        viewCache.pop();
        channel.pop();
        cursorChannel.pop();
      }

    }

    @Override
    protected boolean store(R val)
    throws AccessException
    { return false;
    }

  }  
  
}

