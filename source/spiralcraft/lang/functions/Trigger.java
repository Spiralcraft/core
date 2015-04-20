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
 * <p>Detects a change in a watched expression and performs an resulting action 
 * </p>
 * 
 */
public class Trigger<S>
  extends Accumulator<S,S>
{
  private Expression<Void> action;
  
  public Trigger(Expression<Void> action)
  { 
    this.action=action;
  }

  
  @Override
  protected Context<S> newContext(
    Channel<S> source,
    Focus<?> focus)
    throws BindException
  { 
    return new WatchContext
      (source,focus,source.getReflector());
  }
  


  
  class WatchContext
    extends Context<S>
  {
    private final Channel<Void> actionChannel;
    
    public WatchContext
     (Channel<S> source
     ,Focus<?> focus
     ,Reflector<S> type
     )
      throws BindException
    { 
      super(source,focus,type);
      
      if (!focus.isContext(source))
      { focus=focus.chain(source);
      }
      focus=focus.telescope(stateDataChannel);
      actionChannel=focus.<Void>bind(action);
      
    }
    
    
    @Override
    protected void update(ViewState<S> state)
    { 
      S val=source.get();
      if (state.data!=val && (state.data==null || !state.data.equals(val)))
      { 
        actionChannel.get();
      }
      state.data=val;
      
    }

    @Override
    protected S latest(
      ViewState<S> state)
    { return state.data;
    }

    @Override
    protected boolean reset(ViewState<S> state,S val)
    { 
      state.data=val;
      return true;
    }

  }
  
  
    
}

class TriggerState
{
}
