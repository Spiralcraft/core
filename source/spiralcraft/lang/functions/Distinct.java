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



import java.lang.reflect.Array;
import java.util.LinkedHashSet;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.ArrayReflector;
import spiralcraft.lang.spi.Accumulator;
import spiralcraft.lang.spi.ViewState;

/**
 * <p>Collects a set of the distinct values in the sequence
 * </p>
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
public class Distinct<T>
  extends Accumulator<T[],T>
{

  
  @Override
  protected Context<LinkedHashSet<T>> newContext(
    Channel<T> source,
    Focus<?> focus)
    throws BindException
  { 
    return new DistinctContext(source,focus);
  }
  

  
  class DistinctContext
    extends Context<LinkedHashSet<T>>
  {
    
    public DistinctContext
     (Channel<T> source
     ,Focus<?> focus
     )
      throws BindException
    { 
      super(source,focus);
    }

    
    @Override
    protected Reflector<T[]> resolveResultReflector()
    { return ArrayReflector.getInstance(source.getReflector());
    }
    
    @Override
    protected void update(ViewState<LinkedHashSet<T>> state)
    {
      T val=source.get();

      if (val!=null)
      {
        if (state.data==null)
        { state.data=new LinkedHashSet<T>();
        }
        state.data.add(val);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T[] latest(
      ViewState<LinkedHashSet<T>> state)
    { 
      return  state.data.toArray
        (
          (T[]) Array.newInstance
            (source.getReflector().getContentType(),state.data.size())
        );
    }

    @Override
    protected boolean reset(ViewState<LinkedHashSet<T>> state,T[] val)
    { 
      state.data=new LinkedHashSet<T>();
      for (T item:val)
      { state.data.add(item);
      }
      return true;
    }

  }
  
  
    
}
