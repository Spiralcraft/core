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
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Accumulator;
import spiralcraft.lang.spi.ViewState;

/**
 * <p>Concatenates the values in a sequence of collections
 * </p>
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
public class Concatenate<C>
  extends Accumulator<C,C>
{

  
  @Override
  protected Context<C> newContext(
    Channel<C> source,
    Focus<?> focus)
    throws BindException
  { 
    return new ConcatenateContext(source,focus);
  }
  

  
  class ConcatenateContext
    extends Context<C>
  {
    
    private CollectionDecorator<C,?> collector;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ConcatenateContext
     (Channel<C> source
     ,Focus<?> focus
     )
      throws BindException
    { 
      super(source,focus);
      collector=source.<CollectionDecorator>decorate(CollectionDecorator.class);
      if (collector==null)
      { throw new BindException("Not a collection "+source.getReflector());
      }
    }

    
    @Override
    protected Reflector<C> resolveResultReflector()
    { return source.getReflector();
    }
    
    @Override
    protected void update(ViewState<C> state)
    {
      C val=source.get();

      if (val!=null)
      {
        if (state.data==null)
        { state.data=collector.newCollection();
        }
        state.data=collector.addAll(state.data,val);
      }
    }

    @Override
    protected C latest(
      ViewState<C> state)
    { 
      return  state.data;
    }

    @Override
    protected boolean reset(ViewState<C> state,C val)
    { 
      state.data=collector.addAll(collector.newCollection(),val);
      return true;
    }

  }
  
  
    
}
