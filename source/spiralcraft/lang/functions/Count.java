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



import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.Accumulator;
import spiralcraft.lang.spi.ViewState;

/**
 * Computes the number of non-null values in a sequence
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
public class Count
  extends Accumulator<Integer,Object>
{

  
  @Override
  protected Context<Integer> newContext(
    Channel<Object> source,
    Focus<?> focus)
    throws BindException
  { return new CountContext(source,focus);
  }
  

  
  class CountContext
    extends Context<Integer>
  {
    
    public CountContext
     (Channel<Object> source
     ,Focus<?> focus
     )
      throws BindException
    { super(source,focus);
    }

    @Override
    protected Reflector<Integer> resolveResultReflector()
    { return BeanReflector.getInstance(Integer.class);
    }
    
    @Override
    protected void update(ViewState<Integer> state)
    {
      Object val=source.get();

      if (val!=null)
      {
        if (state.data==null)
        { state.data=1;
        }
        else
        { state.data++;
        }
      }
    }

    @Override
    protected Integer latest(
      ViewState<Integer> state)
    { return state.data;
    }

    @Override
    protected boolean reset(ViewState<Integer> state,Integer val)
    { 
      state.data= val;
      return true;
    }

  }
  
  
    
}
