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

import java.util.logging.Logger;

import spiralcraft.builder.LifecycleException;
import spiralcraft.log.ClassLogger;
import spiralcraft.time.Clock;

/**
 * <p>An asychronous Service that does work in its own continuously
 *   running Thread.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class ThreadService
  extends ServiceAdapter
  implements Runnable
{

  private static final Logger log
    =ClassLogger.getInstance(ThreadService.class);
  
  protected Thread thread;
  private volatile boolean stop=true;
  private boolean stopOnError;
  private int runIntervalMs=1000;
  private long lastRun;
  protected boolean debug;
  
  
  
  public void setStopOnError(boolean stopOnError)
  { this.stopOnError=stopOnError;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * <p>The minimum amount of time between the start of successive calls
   *   to the to runOnce() method. If the runOnce() method takes longer
   *   than this interval to complete, it will be called again immediately
   * </p>
   * 
   * @param runIntervalMs
   */
  public void setRunIntervalMs(int runIntervalMs)
  { this.runIntervalMs=runIntervalMs;
  }
  
  @Override
  public synchronized void start()
    throws LifecycleException
  {
    if (thread!=null)
    { throw new IllegalStateException("Already started");
    }
       
    stop=false;
    thread=new Thread(this);
    thread.start();
    log.fine("started");
  }

  @Override
  public synchronized void stop()
  {
    if (stop)
    { return;
    }
    log.fine("stopping");
    stop=true;
    this.notify();
    try
    { this.wait();
    }
    catch (InterruptedException x)
    { x.printStackTrace();
    }
    thread=null;
    log.fine("stopped");

  }
  
  public final void run()
  { 
    while (!stop)
    { 
      try
      { 
        lastRun=Clock.instance().approxTimeMillis();
        if (debug)
        { log.fine("Running...");
        }
        runOnce();
      }
      catch (RuntimeException x)
      { 
        if (stopOnError)
        { stop();
        }
      }
      
      long elapsedTime
        =(Clock.instance().approxTimeMillis()-lastRun);
      if (elapsedTime<runIntervalMs)
      { 
        try
        { 
          if (debug)
          { log.fine("Sleeping for "+(runIntervalMs-elapsedTime)+"ms");
          }
          synchronized (this)
          { wait(runIntervalMs-elapsedTime);
          }
        }
        catch (InterruptedException x)
        { 
          x.printStackTrace();
          stop();
        }
      }
    }
    synchronized (this)
    { notify();
    }
  }
  
  protected abstract void runOnce();

}
