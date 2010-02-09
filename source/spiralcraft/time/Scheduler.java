//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.time;

//import java.util.logging.Logger;

//import spiralcraft.log.ClassLogger;

import spiralcraft.pool.ThreadPool;
import spiralcraft.util.thread.ThreadLocalStack;

/**
 * Schedules Runnable items to be executed at
 *   specific points in time.
 */
public class Scheduler
{
//  private static final Logger log
//    =ClassLogger.getInstance(Scheduler.class);

  private static volatile int NEXT_ID=-0;
  private static final Scheduler _INSTANCE=new Scheduler();
  
  private static ThreadLocalStack<Scheduler> stack
    =new ThreadLocalStack<Scheduler>(true)
    {
       @Override
       public Scheduler defaultValue()
       { return _INSTANCE;
       }
    };
  
  public static void push(Scheduler scheduler)
  { stack.push(scheduler);
  }
  
  public static void pop()
  { stack.pop();
  }
  
  public static Scheduler instance()
  { return stack.get();
  }
    
  private ScheduledItem _nextItem;
  private final Object _sync=new Object();
  private int id=NEXT_ID++;
  private final Thread _thread
    =new Thread(new Dispatcher(),"scheduler-"+id);
  
  private ThreadPool _pool
    =new ThreadPool();
  { _pool.setThreadNamePrefix("scheduler-"+id);
  }
  
  private boolean _started=false;
  private volatile boolean shutdown=false;
  



  public Scheduler()
  { 
    _thread.setDaemon(true);
    _thread.start();
    
    Runtime.getRuntime().addShutdownHook
      (new Thread()
      {
        @Override
        public void run()
        { shutdown=true;
        }
      }
      );
  }

  public void scheduleIn(Runnable runnable,long msFromNow)
  { scheduleAt(runnable,Clock.instance().approxTimeMillis()+msFromNow);
  }

  public void scheduleNow(Runnable runnable)
  { scheduleIn(runnable,0);
  }

  public void scheduleAt(Runnable runnable,long duetime)
  {
    synchronized (_sync)
    {

      ScheduledItem thisItem=new ScheduledItem(runnable,duetime);

      if (_nextItem==null || duetime<_nextItem.duetime)
      { 
        // Insert earlier due task at the head of the queue.
        // Notify dispatcher.
        thisItem.nextItem=_nextItem;
        _nextItem=thisItem;
        _sync.notify();
      }
      else
      {
        ScheduledItem next=_nextItem;
        while (next!=null)
        {
          if (next.nextItem==null || duetime<next.nextItem.duetime)
          { 
            // Insert at this point.
            thisItem.nextItem=next.nextItem;
            next.nextItem=thisItem;
            next=null;
          }
          else
          { next=next.nextItem;
          }
        }
      }
    }
  }

  public boolean cancel(Runnable runnable)
  {
    synchronized (_sync)
    {
      ScheduledItem last=null;
      ScheduledItem next=_nextItem;
      while (next!=null)
      { 
        if (next.runnable==runnable)
        { 
          if (last!=null)
          { last.nextItem=next.nextItem;
          }
          else
          { _nextItem=next.nextItem;
          }
          return true;
        }
        else
        { 
          last=next;
          next=next.nextItem;
        }
      }
      return false;
    }
  }

  protected void runNext()
    throws InterruptedException
  {
    ScheduledItem next=null;
    synchronized (_sync)
    {
      if (shutdown)
      { return;
      }
      
      if (!_started)
      { 
        _pool.start();
        _started=true;
      }

      long time=0;
      while (_nextItem==null 
             || _nextItem.duetime> (time=Clock.instance().approxTimeMillis()) 
             )
      { 
        if (_nextItem==null)
        { _sync.wait();
        }
        else
        { _sync.wait(_nextItem.duetime-time);
        }
      }
      next=_nextItem;
      _nextItem=_nextItem.nextItem;
    }

    if (next!=null)
    {
      try
      { dispatch(next.runnable);
      }
      catch (Throwable x)
      { x.printStackTrace();
      }
    }
  }

  protected void dispatch(Runnable runnable)
  { 
    
    try
    { _pool.run(runnable);
    }
    catch (InterruptedException x)
    { throw new RuntimeException("Timed out waiting for thread pool",x);
    }
  }

  
  class Dispatcher
    implements Runnable
  {
    public void run()
    {
      try
      {
        while (!shutdown)
        { runNext();
        }
      }
      catch (InterruptedException x)
      { x.printStackTrace(); 
      }
    }
  }

  private class ScheduledItem
  {
    public final long duetime;
    public final Runnable runnable;
    public ScheduledItem nextItem;

    public ScheduledItem(Runnable runnable,long duetime)
    { 
      this.duetime=duetime;
      this.runnable=runnable;
    }

  }

  
}
