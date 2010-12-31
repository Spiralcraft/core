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
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Accumulator;
import spiralcraft.lang.spi.ViewState;

/**
 * <p>Uses an Expression to accumulate a result from an input sequence.
 * </p>
 * 
 * <p>Within the expression, the accumulated state is referenced as the
 *   subject (".") and the input is referenced as the immediate containing 
 *   context ("..")
 * </p>
 *   
 * <p>For example, the following expression computes the sum of a set of
 *   numbers:  numbers$[ [*fn:Induce{`.+..`}] ]
 * </p>
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
public class Induce<R,S>
  extends Accumulator<R,S>
{

  private Expression<R> x;
  private Reflector<R> type;
  
  public Induce(Reflector<R> type,Expression<R> x)
  { 
    this.type=type;
    this.x=x;
  }

  public Induce(Expression<R> x)
  { this.x=x;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected Context<R> newContext(
    Channel<S> source,
    Focus<?> focus)
    throws BindException
  { 
    return new InduceContext
      (source,focus,type!=null?type:(Reflector<R>) source.getReflector());
  }
  


  
  class InduceContext
    extends Context<R>
  {
    private final Channel<R> channel;
    
    public InduceContext
     (Channel<S> source
     ,Focus<?> focus
     ,Reflector<R> type
     )
      throws BindException
    { 
      super(source,focus,type);
      
      if (!focus.isContext(source))
      { focus=focus.chain(source);
      }
      focus=focus.telescope(stateDataChannel);
      channel=focus.<R>bind(x);
      
    }
    
    protected Reflector<R> resolveResultType()
    { return stateDataChannel.getReflector();
    }
    
    @Override
    protected void update(ViewState<R> state)
    { 
      state.data=channel.get();
    }

    @Override
    protected R latest(
      ViewState<R> state)
    { return state.data;
    }

    @Override
    protected boolean reset(ViewState<R> state,R val)
    { 
      state.data= val;
      return true;
    }

  }
  
  
    
}
