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
package spiralcraft.service;


import spiralcraft.app.spi.AbstractComponent;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.CachedChannel;
import spiralcraft.task.TaskScheduler;
import spiralcraft.time.Scheduler;

/**
 * Manages a set of TaskSchedulers to run contextual time-based tasks
 * 
 * @author mike
 *
 */
public class SchedulerService<Tcontext>
  extends AbstractComponent
  implements Service
{

  private TaskScheduler[] schedulers;
  private Binding<Tcontext> contextX;
  private CachedChannel<Tcontext> context;
  private Scheduler scheduler=new Scheduler();
  
  public void setSchedulers(final TaskScheduler[] schedulers)
  { this.schedulers=schedulers;
  }  
  
  public void setContextX(Binding<Tcontext> contextX)
  { this.contextX=contextX;
  }
  
  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws BindException
  { 
    if (contextX!=null)
    { 
      contextX.bind(chain);
      
      context=new CachedChannel<Tcontext>(contextX);
      chain=chain.chain(context);
    }
    return chain;
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> chain)
    throws BindException
  { 

    if (schedulers!=null)
    { 
      for (TaskScheduler scheduler:schedulers)
      { scheduler.bind(chain);
      }
    }
    return chain;
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    if (context!=null)
    { context.get();
    }
    if (schedulers!=null)
    {
      for (TaskScheduler scheduler:schedulers)
      { 
        scheduler.setScheduler(this.scheduler);
        scheduler.start();
      }
    }
  }
  
  @Override
  public void stop()
    throws LifecycleException
  {
    if (schedulers!=null)
    {
      for (TaskScheduler scheduler:schedulers)
      { scheduler.stop();
      }
    }
    
  }
}
