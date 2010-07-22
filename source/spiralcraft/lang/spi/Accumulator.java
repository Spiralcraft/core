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
package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.log.ClassLog;

/**
 * 
 * <p>Base class for accumulator functions that perform a running computation 
 *   over the lifetime of a client-defined state
 * </p>
 *  
 * <p>This is a ChannelFactory which will implement the function when
 *   bound as an ObjectLiteral into an expression- ie. expr.[*myns:MyFn]
 *   
 *
 * <p>The state is managed by the nearest ViewCache object in the Focus chain
 * </p>
 * 
 * <p>Extension is accomplished by subclassing the inner Context class and
 *   overriding newContext. By default, both the running state and result 
 *   Reflectors are the same as the source. These types are controlled by the
 *   resolveResultReflector and resolveStateReflector methods, which can
 *   be overriddenl
 * </p>
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
public abstract class Accumulator<Tresult,Tsource>
  implements ChannelFactory<Tresult,Tsource>
{

  private final ClassLog log
    =ClassLog.getInstance(getClass());
  
  protected abstract Context<?> newContext
    (Channel<Tsource> source
    ,Focus<?> focus
    )
    throws BindException;
  
  @Override
  public Channel<Tresult> bindChannel(
    Channel<Tsource> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    return newContext(source,focus).result;
    
  }

  protected abstract class Context<Tstate>
  {
    private final Reflector<Tresult> resultReflector;
    private final Reflector<Tstate> stateReflector;
    private final Channel<ViewState<Tstate>> stateChannel;
    protected final Channel<Tstate> stateDataChannel;
    protected final Channel<Tsource> source;
    private final Channel<Tresult> result;

    public Context
      (Channel<Tsource> source
      ,Focus<?> focus
      )
      throws BindException
    {
      this.source=source;
      
      ViewCache cache=ViewCache.find(focus);
      if (cache==null)
      { 
        throw new BindException
          ("Aggregate function must be contained in an appropriate context ");
      }
      
      stateReflector=resolveStateReflector();
      stateChannel=cache.bind(stateReflector);
      stateDataChannel=stateChannel.resolve(focus,"data",null);
      resultReflector=resolveResultReflector();
      
      result=new AccumulatorChannel();
      result.setContext(stateChannel.getContext());
    }
    
    @SuppressWarnings("unchecked")
    protected Reflector<Tresult> resolveResultReflector()
    { return (Reflector<Tresult>) source.getReflector();
    }
    
    @SuppressWarnings("unchecked")
    protected Reflector<Tstate> resolveStateReflector()
    { return (Reflector<Tstate>) source.getReflector();
    }
    
    protected abstract Tresult latest(ViewState<Tstate> state);
      
    protected abstract void update(ViewState<Tstate> state);

    /**
     * <p>Called when the current subsequence ends. 
     * </p>
     * 
     * <p>The default implementation does nothing, relying on the 
     *   update function to keep the running data up-to-date.
     *   
     * @param state
     */
    protected void checkpoint(ViewState<Tstate> state)
    { 
    }
    
    protected abstract boolean reset(ViewState<Tstate> state,Tresult val);

    class AccumulatorChannel
      extends AbstractChannel<Tresult>
    {
      public AccumulatorChannel()
      { super(resultReflector);
      }

      @Override
      protected Tresult retrieve()
      {
        ViewState<Tstate> state=stateChannel.get();
        if (state.frameChanged)
        { 
          state.frameChanged=false;
          if (debug)
          { log.fine("Updating "+state);
          }
          update(state);
        }
        
        if (state.checkpoint)
        { 
          state.checkpoint=false;
          checkpoint(state);
        }
            
        return latest(state);
      }

      @Override
      protected boolean store(Tresult val)
        throws AccessException
      { 
        ViewState<Tstate> state=stateChannel.get();
        state.checkpoint=false;
        state.frameChanged=false;
        return reset(state,val);
      }
      
    };
      
    
  }

}
