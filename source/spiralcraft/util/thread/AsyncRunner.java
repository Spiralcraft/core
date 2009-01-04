//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.util.thread;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>A Runnable which runs asynchronously via the Lifecycle interface
 * </p>
 * 
 * @author mike
 *
 */
public abstract class AsyncRunner
  implements Lifecycle, Runnable
{
  private static volatile int ID=0;
  
  protected final ClassLog log
    =ClassLog.getInstance(getClass());
  
  protected String name="async-"+ID+++"-"+(getClass().getSimpleName());
  
  protected ThreadGroup threadGroup;
  protected Thread thread;
  protected volatile boolean started;
  protected long timeout=0;
  protected volatile boolean stop=true;
  
  protected boolean debug=false;
  
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  
  @Override
  public synchronized final void start()
    throws LifecycleException
  {
    if (debug)
    { log.fine("Start requested");
    }
    
    if (started)
    { throw new IllegalStateException("Already started");
    }
    
    if (threadGroup==null)
    { threadGroup=Thread.currentThread().getThreadGroup();
    }
    stop=false;
    thread=createThread();
    thread.start();
    started=true;

    if (debug)
    { log.fine("Started");
    }

  }

  @Override
  public synchronized final void stop()
    throws LifecycleException
  {
    if (debug)
    { log.fine("Stop requested");
    }
    
    if (!started)
    { throw new IllegalStateException("Not started");
    }
    stop=true;
    thread.interrupt();
    try
    { 
      if (debug)
      { log.fine("Joining...");
      }
      thread.join(timeout);
    }
    catch (InterruptedException x)
    {
      if (debug)
      { log.log(Level.DEBUG,"Interrupted",x);
      }
    }
    
    started=false;

    if (debug)
    { log.fine("Stopped");
    }

  }

  protected Thread createThread()
  { return new Thread(threadGroup,this,name);
  }
  
  protected final boolean shouldStop()
  { return stop;
  }
  
  @Override
  public abstract void run();


}
