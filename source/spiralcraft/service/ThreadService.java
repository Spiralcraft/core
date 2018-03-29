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
import spiralcraft.lang.Binding;
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
  private volatile boolean stopping=true;
  private boolean stopOnError;
  private int runIntervalMs=1000;
  private long lastRun;
  private boolean autoStart=true;
  private boolean startRequested;
  
  private Binding<Void> beforeStart;
  private Binding<Void> afterStart;
  private Binding<Void> beforeStop;
  private Binding<Void> afterStop;

  /**
   * Invoke before starting the component
   * 
   * @param binding
   */
  public void setBeforeStart(Binding<Void> binding)
  {
    this.removeExportContextual(beforeStart);
    this.beforeStart=binding;
    this.addExportContextual(beforeStart);
  }

  /**
   * Invoke after starting the component
   * 
   * @param binding
   */
  public void setAfterStart(Binding<Void> binding)
  {
    this.removeExportContextual(afterStart);
    this.afterStart=binding;
    this.addExportContextual(afterStart);
  }

  /**
   * Invoke before stopping the component
   * 
   * @param binding
   */
  public void setBeforeStop(Binding<Void> binding)
  {
    this.removeExportContextual(beforeStop);
    this.beforeStop=binding;
    this.addExportContextual(beforeStop);
  }

  /**
   * Invoke after stopping the component
   * 
   * @param binding
   */
  public void setAfterStop(Binding<Void> binding)
  {
    this.removeExportContextual(afterStop);
    this.afterStop=binding;
    this.addExportContextual(afterStop);
  }

  /**
   * Stop the service when an error is encountered
   * 
   * @param stopOnError
   */
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

  /**
   * When autoStart is true, the service will start when the Lifecycle method is
   *   invoked by parent componenents. When false, the service will only start
   *   when the startService() method is invoked.
   * 
   * @param autoStart
   */
  public void setAutoStart(boolean autoStart)
  { this.autoStart=autoStart;
  }
  
  /**
   * Start service on demand outside the Lifecycle pattern when 
   *   autoStart is false
   */
  public synchronized void startService()
    throws LifecycleException
  { 
    this.startRequested=true;
    this.start();
  }
  
  /**
   * Stop the service on demands outside the Lifecycle pattern
   *    
   * @throws LifecycleException
   */
  public synchronized void stopService()
      throws LifecycleException
  {
    this.startRequested=false;
    this.stop();
  }
   
    
  @Override
  public synchronized void start()
    throws LifecycleException
  {
    if (autoStart || startRequested)
    {
      if (thread!=null)
      { throw new IllegalStateException("Already started");
      }
      
      if (beforeStart!=null) { if (!safeGet(beforeStart)) { return;} }   
      stopping=false;
      thread=new Thread(this);
      thread.start();
      log.log(Level.FINE,("started"));
      super.start();
      startRequested=false;
      if (afterStart!=null) { safeGet(afterStart); }   
    }
  }

  @Override
  public synchronized void stop()
    throws LifecycleException
  {
    if (stopping)
    { return;
    }
    
    log.log(Level.FINE,"stopping");
    stopping=true;
    if (beforeStop!=null) { safeGet(beforeStop); }   
    this.notify();
    try
    { this.wait();
    }
    catch (InterruptedException x)
    { x.printStackTrace();
    }
    thread=null;
    log.log(Level.FINE,"stopped");
    if (afterStop!=null) { safeGet(afterStop); }   

  }
  
  @Override
  public final void run()
  { 
    while (!stopping)
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
      if (!stopping && elapsedTime<runIntervalMs)
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
  
  private boolean safeGet(Binding<?> binding)
  {
    try
    { binding.get();
    }
    catch (Exception x)
    { 
      log.log(Level.SEVERE,x.getMessage(),x);
      return false;
    }
    return true;
  }
  protected abstract void runOnce();

}
