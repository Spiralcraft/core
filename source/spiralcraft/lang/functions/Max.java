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
import spiralcraft.lang.spi.Accumulator;
import spiralcraft.lang.spi.ViewState;

/**
 * Computes the maximum non-null value in a sequence 
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
@SuppressWarnings("unchecked")
public class Max<T>
  extends Accumulator<Comparable,Comparable>
{

  
  @Override
  protected Context<Comparable> newContext(
    Channel<Comparable> source,
    Focus<?> focus)
    throws BindException
  { return new MaxContext(source,focus);
  }
  

  
  class MaxContext
    extends Context<Comparable>
  {
    
    public MaxContext
     (Channel<Comparable> source
     ,Focus<?> focus
     )
      throws BindException
    { super(source,focus);
    }

    
    @Override
    protected void update(ViewState<Comparable> state)
    {
      Comparable val=source.get();

      if (val!=null)
      {
        if (state.data==null)
        { state.data=val;
        }
        else
        { 
          if (state.data.compareTo(val)<0)
          { state.data=val;
          }
        }
      }
    }

    @Override
    protected Comparable latest(
      ViewState<Comparable> state)
    { return state.data;
    }

    @Override
    protected boolean reset(ViewState<Comparable> state,Comparable val)
    { 
      state.data= val;
      return true;
    }

  }
  
  
    
}
