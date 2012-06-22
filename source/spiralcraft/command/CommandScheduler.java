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
package spiralcraft.command;

import java.util.Date;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.time.Instant;
import spiralcraft.time.Recurrent;
import spiralcraft.time.Scheduler;
import spiralcraft.vfs.context.ContextResourceMap;

/**
 * Schedules a command to run periodically. Designed to be subclassed by or
 *   included in another object which provides context.
 * 
 * @author mike
 *
 */
@SuppressWarnings({"rawtypes"})
public class CommandScheduler
  implements Lifecycle
{

  private static final ClassLog log
    =ClassLog.getInstance(CommandScheduler.class);
  
  private CommandFactory factory;
  private Recurrent recurrent;
  private long period;
  private volatile long mark;
  private boolean regular=false;
  private volatile boolean started=false;
  private Scheduler scheduler=Scheduler.instance();
  private boolean debug;
  private boolean delay;
  protected ContextResourceMap resourceMap;
  protected ClassLoader contextClassLoader
    =Thread.currentThread().getContextClassLoader();
  
  /**
   * Construct a default CommandScheduler for external configuration
   */
  public CommandScheduler()
  {
  }
  
  /**
   * Construct a CommandScheduler that will run the specified task
   *   every <i>period</i> milliseconds.
   *   
   * @param period
   * @param task
   */
  public CommandScheduler(long period,final Runnable task)
  {
    
    this.period=period;
    this.factory
      =new AbstractCommandFactory<Void,Void,Void>()
      {

        @Override
        public Command<Void, Void, Void> command()
        { 
          return new CommandAdapter<Void,Void,Void>()
          { 
            @Override
            protected void run()
            { task.run();
            }
          };
        }
      };
      
  }
  
  private final Runnable runnable
    =new Runnable()
  {
    @Override
    public void run()
    {
      synchronized (CommandScheduler.this)
      {
        if (!started)
        { return;
        }
      }
      
      if (resourceMap!=null)
      { resourceMap.push();
      }
      ClassLoader lastClassLoader
        =Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(contextClassLoader);
      try
      {
        Command command=factory.command();
        command.execute();
        if (command.getException()!=null)
        { 
          log.log
            (Level.WARNING,"Scheduled command threw exception "
            ,command.getException()
            );
        }
      }
      catch (Exception x)
      {
        log.log
        (Level.WARNING,"Scheduled command threw exception "
        ,x
        );
      }
      finally
      {
        Thread.currentThread().setContextClassLoader(lastClassLoader);
        if (resourceMap!=null)
        { resourceMap.pop();
        }
      }
      synchronized (CommandScheduler.this)
      {
        if (started)
        {
          if (!regular)
          { 
            // Compute period from end of current run
            mark=System.currentTimeMillis()+period;
          }
          reschedule();
        }
      }
    }
  };
  
  /**
   * The Scheduler which will manage the thread pool for this client
   * 
   * @param scheduler
   */
  public void setScheduler(Scheduler scheduler)
  { this.scheduler=scheduler;
  }
  
  public void setCommandFactory(CommandFactory factory)
  { this.factory=factory;
  }
  
  /**
   * A Recurrent implementation to determine the next scheduled Instant
   * 
   * @param recurrent
   */
  public void setRecurrent(Recurrent recurrent)
  { 
    this.recurrent=recurrent;
    this.regular=true;
  }
  
  public void setPeriod(long period)
  { this.period=period;
  }
  
  /**
   * Delay the first run instead of executing it on start
   * 
   * @param delay
   */
  public void setDelay(boolean delay)
  { this.delay=delay;
  }
  
  public void setRegular(boolean regular)
  { this.regular=regular;
  }
  
  public void setMark(long mark)
  { this.mark=mark;
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  @Override
  public synchronized void start()
    throws LifecycleException
  { 
    if (!started)
    {
      if (resourceMap==null)
      { resourceMap=ContextResourceMap.get();
      }
      started=true;
      reschedule();
    }
    else
    { log.warning("Multiple starts");
    }
  }

  @Override
  public synchronized void stop()
    throws LifecycleException
  { 
    started=false;
    cancel();
  }
  
  public synchronized void trigger()
  { 
    if (cancel())
    { reschedule();
    }
  }
  
  private synchronized boolean cancel()
  { return scheduler.cancel(runnable);
  }
  
  private synchronized void reschedule()
  {
    
    long now=System.currentTimeMillis();
    if (mark==0 && delay)
    {
      if (recurrent==null)
      { mark=now+period;
      }
    }
    
    if (now>=mark)
    { 
      // Queued mark has passed, generate a new one
      
      if (!regular)
      { scheduler.scheduleNow(runnable);
      }
      else
      { 
        if (recurrent==null)
        { 
          if (mark<now-period*100)
          { 
            // Reset, too far gone
            mark=now;
          }
          else
          {
            // Isochronous
            while (now>=mark)
            { mark=mark+period;
            }
          }
        }
        else
        { 
          // Calendared
          mark=recurrent.next(new Instant()).getOffsetMillis();
        }
        
        if (debug)
        { log.fine("Scheduling regular item for "+mark+" ("+new Date(mark)+")");
        }
        scheduler.scheduleAt(runnable,mark);
      }
    }
    else
    { 
      // Mark is in the future, schedule for queued mark
      if (debug)
      { log.fine("Scheduling for queued time "+mark+" ("+new Date(mark)+")");
      }
      scheduler.scheduleAt(runnable,mark);
    }
    
    if (regular)
    { 
      if (recurrent==null)
      {
        // Queue up the next Isochronous mark
        mark=mark+period;
      }
      else
      { 
        // Queue up the next calendared mark
        mark=recurrent.next(new Instant(mark)).getOffsetMillis();
      }
      if (debug)
      { log.fine("Queued next run for  "+mark+" ("+new Date(mark)+")");
      }
    }
    
  }
  
}
