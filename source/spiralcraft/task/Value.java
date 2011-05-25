//
// Copyright (c) 2008,2009 Michael Toth
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
package spiralcraft.task;

import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.Context;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * Integrates an arbitrary component into the Scenario chain
 * 
 * @author mike
 *
 * @param <T>
 * @param <R>
 */
public class Value<Tresult>
  extends Chain<Void,Tresult>
{

  
  private Tresult value;
  private Contextual contextual;
  private Context context;
  private Lifecycle lifecycle;
  
  { addChainResult=true;
  }
  
  public void setValue(Tresult value)
  { 
    this.value=value;
    if (value instanceof Contextual)
    { contextual=(Contextual) value;
    }
    if (value instanceof Context)
    { context=(Context) value;
    }
    if (value instanceof Contextual)
    { lifecycle=(Lifecycle) value;
    }
  }
  
  @Override
  protected Task task()
  {
    
    
    return new ChainTask()
    {
      @Override
      protected void work()
        throws InterruptedException
      {         
        if (context!=null)
        { context.push();
        }
        try
        { super.work();
        }
        finally
        {
          if (context!=null)
          { context.pop();
          }
        }
        
      }      
    };
    
  }

  @Override
  public void start()
    throws LifecycleException
  {
    if (lifecycle!=null)
    { lifecycle.start();
    }
    super.start();
  }
  
  @Override
  public void stop()
    throws LifecycleException
  {
    super.stop();
    if (lifecycle!=null)
    { lifecycle.stop();
    }
  }

  
  @Override
  protected void bindChildren(
    Focus<?> focusChain)
    throws ContextualException
  {
    if (contextual!=null)
    { focusChain=contextual.bind(focusChain);
    }
    else
    { focusChain=focusChain.chain(new SimpleChannel<Tresult>(value,true));
    }
    super.bindChildren(focusChain);
  }

}
