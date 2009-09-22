//
// Copyright (c) 1998,2008 Michael Toth
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
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.util.lang.ClassUtil;

/**
 * Utility to build a projection Channel on an Iterable
 * 
 * @author mike
 *
 * @param <I>
 * @param <P>
 */
public class IterationProjector<I,P,R>
{

  public final Channel<R> result;
  
  protected final IterationDecorator<I[],I> iterable;
    
  protected final ThreadLocalChannel<I> channel;
        
  protected final Focus<I> telefocus;
    
  protected final Channel<P> projectionChannel;  
  
  @SuppressWarnings("unchecked")
  public IterationProjector
    (Channel<I[]> source
    ,Focus<?> focus
    ,Expression<P> projection
    )
    throws BindException
  {
    iterable
      =source.getReflector()
        .<IterationDecorator>decorate(source,IterationDecorator.class);
    if (iterable==null)
    { throw new BindException("Source is not iterable: "+source);
    }
    channel
      =new ThreadLocalChannel<I>(iterable.getComponentReflector());
        
    telefocus=focus.telescope(channel);
    
    projectionChannel=telefocus.bind(projection);
    
    result=new AbstractChannel<R>
      (containerReflector(projectionChannel))
    {

      @Override
      protected R retrieve()
      {
        ArrayList<P> output=new ArrayList<P>();
        channel.push(null);
        try
        {
          for (I item : iterable)
          { 
            channel.set(item);
            output.add(projectionChannel.get());
          }
          return createResult(output); 
        }
        finally
        { channel.pop();
        }
        
      }

      @Override
      protected boolean store(
        R val)
        throws AccessException
      { return false;
      }
    };
      
  }
  
  @SuppressWarnings("unchecked")
  public  R createResult(ArrayList<P> output)
  {
    Class pclass=projectionChannel.getContentType();
    if (pclass.isPrimitive())
    { pclass=ClassUtil.boxedEquivalent(pclass);
    }
    return (R) 
      output.toArray((Object[]) Array.newInstance(pclass,output.size()));
  }
  
  @SuppressWarnings("unchecked")
  public Reflector<R> containerReflector(Channel<P> projection)
    throws BindException
  { 
    return (Reflector<R>) 
      ArrayReflector.getInstance(projection.getReflector());
  }
  
}
