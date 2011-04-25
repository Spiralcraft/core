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


import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.common.LifecycleException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.time.Clock;

/**
 * <p>An asychronous Service that does work in its own continuously
 *   running Thread at a specified interval.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class ThreadService
  extends AbstractComponent
  implements Runnable
{

  
  protected Thread thread;
  private volatile boolean stop=true;
  private boolean stopOnError;
  private int runIntervalMs=1000;
  private long lastRun;
  
  
  
  public void setStopOnError(boolean stopOnError)
  { this.stopOnError=stopOnError;
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
    log.log(Level.FINE,("started"));
    super.start();
  }

  @Override
  public synchronized void stop()
    throws LifecycleException
  {
    if (stop)
    { return;
    }
    log.log(Level.FINE,"stopping");
    stop=true;
    this.notify();
    try
    { this.wait();
    }
    catch (InterruptedException x)
    { x.printStackTrace();
    }
    thread=null;
    log.log(Level.FINE,"stopped");

  }
  
  @Override
  public final void run()
  { 
    while (!stop)
    { 
      try
      { 
        lastRun=Clock.instance().approxTimeMillis();
        if (logLevel.canLog(Level.FINE))
        { log.log(Level.FINE,"Running...");
        }
        runOnce();
      }
      catch (Throwable x)
      { 
        log.log(ClassLog.SEVERE,"Uncaught exception in service thread",x);
        x.printStackTrace();
        if (stopOnError)
        { 
          try
          { stop();
          }
          catch (LifecycleException x2)
          {  log.log(ClassLog.SEVERE,"Exception stopping service",x2);
          }
        }
      }
      
      long elapsedTime
        =(Clock.instance().approxTimeMillis()-lastRun);
      if (!stop && elapsedTime<runIntervalMs)
      { 
        try
        { 
          if (logLevel.canLog(Level.FINE))
          { log.log
              (Level.FINE,"Sleeping for "+(runIntervalMs-elapsedTime)+"ms");
          }
          synchronized (this)
          { wait(runIntervalMs-elapsedTime);
          }
        }
        catch (InterruptedException x)
        { 
          x.printStackTrace();
          try
          { stop();
          }
          catch (LifecycleException x2)
          { log.log(ClassLog.SEVERE,"Exception stopping service",x2);
          }
        }
      }
    }
    synchronized (this)
    { notify();
    }
  }
  
  protected abstract void runOnce();

}
