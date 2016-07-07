//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.app.kit;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;
import spiralcraft.app.Component;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.State;
import spiralcraft.app.kit.ValueState;
import spiralcraft.common.ContextualException;

/**
 * <p>A Component implementation geared towards incorporating a part of the
 *   application model into the Component hierarchy.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class AbstractModelComponent<T>
  extends AbstractComponent
{
  protected ThreadLocalChannel<T> channel;
  private Channel<T> source;
  private boolean writeThrough;
    

  
  @Override
  protected void addHandlers()
  {
    super.addHandlers();
    addHandler
      (new AbstractMessageHandler()
        {
          { contextual=true;
          }

          @Override
          protected void doHandler(
            Dispatcher dispatcher,
            Message message,
            MessageHandlerChain next)
          {
            @SuppressWarnings("unchecked")
            ValueState<T> state=(ValueState<T>) dispatcher.getState();
            boolean computed=false;
            T oldValue=null;
            T newValue=null;
            try
            {
              if (state.isNewFrame() || !state.isValid())
              { 
                oldValue=state.getValue();
                newValue=compute(state);
                state.setValue(newValue);
                computed=true;
              }
            }
            catch (Exception x)
            { log.log(Level.WARNING,"Caught exception computing value",x);
            }
            channel.push(state.getValue());
            try
            { 
              if (computed)
              { onCompute(state,oldValue,newValue);
              }
              next.handleMessage(dispatcher,message);
            }
            finally
            { channel.pop();
            }
          }
        }
      );   
  }
  
  /**
   * Override to access local value channel after "compute" takes place
   */
  protected void onCompute(ValueState<T> state,T oldValue,T newValue)
  { 
  }
  
  @Override
  public void setContents(Component[] contents)
  { super.setContents(contents);
  }
  
  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  {
    chain=super.bindImports(chain);
    source=bindSource(chain);
    if (!writeThrough)
    { channel=new ThreadLocalChannel<T>(source.getReflector(),true,source);
    }
    else
    { channel=new ThreadLocalChannel<T>(source,true,true);
    }    
    return chain;
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> chain)
    throws ContextualException
  { return super.bindExports(chain).chain(channel);
  }
  
  /**
   * Whether writes to the Focus subject will be propagated upstream
   * 
   * @param writeThrough
   */
  public void setWriteThrough(boolean writeThrough)
  { this.writeThrough=writeThrough;
  }

  
  /**
   * <p>Override to set up the data source for this FocusElement
   * </p>
   * 
   * @param parentFocus
   * @return The data source that will be cached in the local state
   * @throws BindException
   */
  protected abstract Channel<T> bindSource(Focus<?> focusChain)
    throws BindException;
  
  /**
   * <p>Recompute the current value that will be exported by this FocusElement
   * </p>
   * 
   * @return The value that will be pushed into the context and exported
   *    to children. If the state is non-null, the return value will
   *    be put into the ValueState.
   */
  protected abstract T compute(ValueState<T> state);

  protected Class<? extends State> getStateClass()
  { return ValueState.class;
  }
}
