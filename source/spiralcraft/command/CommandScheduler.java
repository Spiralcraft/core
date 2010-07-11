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

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.time.Instant;
import spiralcraft.time.Recurrent;
import spiralcraft.time.Scheduler;

/**
 * Schedules a command to run periodically. Designed to be subclassed by or
 *   included in another object which provides context.
 * 
 * @author mike
 *
 */
@SuppressWarnings("unchecked")
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
  private final Runnable runnable
    =new Runnable()
  {
    public void run()
    {
      if (!started)
      { return;
      }
      
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
  
  public void setRegular(boolean regular)
  { this.regular=regular;
  }
  
  public void setMark(long mark)
  { this.mark=mark;
  }

  @Override
  public synchronized void start()
    throws LifecycleException
  { 
    started=true;
    reschedule();
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
        
        scheduler.scheduleAt(runnable,mark);
      }
    }
    else
    { 
      // Mark is in the future, schedule for queued mark
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
    }
    
  }
  
}
