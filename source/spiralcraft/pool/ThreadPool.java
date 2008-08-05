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
package spiralcraft.pool;


/**
 * Provides a means for dispatching Runnables on their
 *   own Threads with support for Thread recycling to
 *   conserve resources.
 */
public class ThreadPool
  extends Pool
  implements ResourceFactory
{

  private static int _THREAD_COUNT=0;

  { setResourceFactory(this);
  }

  /**
   * Run the supplied Runnable in its own thread
   */  
  public void run(Runnable runnable)
  {
    PooledThread thread=(PooledThread) checkout();
    thread.start(runnable);
  }

  public Object createResource()
  { 
    PooledThread thread=new PooledThread();
    thread.start();
    return thread;
  }

  public void discardResource(Object resource)
  { ((PooledThread) resource).finish();
  }

  class PooledThread
    extends Thread
  {
    private ClassLoader _contextClassLoader;
    private Runnable _runnable=null;
    private final Object _monitor=new Object();
    private boolean _finished=false;

    public PooledThread()
    { 
      super("PooledThread"+_THREAD_COUNT++);
      setDaemon(true);
    }
    
    public void start(Runnable runnable)
    { 
      synchronized(_monitor)
      { 
        _runnable=runnable;
        _contextClassLoader=getContextClassLoader();
        setContextClassLoader(Thread.currentThread().getContextClassLoader());
        _monitor.notify();
      }
    }

    public void finish()
    { 
      synchronized (_monitor)
      {
        _finished=true;
        _monitor.notify();
      }
    }

    @Override
    public void run()
    {
      try
      {
        while (true)
        {
          synchronized (_monitor)
          {
            while (_runnable==null)
            { 
              if (_finished)
              { return;
              }
              _monitor.wait();
            }
          }

          try
          { _runnable.run();
          }
          catch (Throwable x)
          { x.printStackTrace();
          }

          _runnable=null;
          setContextClassLoader(_contextClassLoader);
          checkin(this);
        }
      }
      catch (InterruptedException x)
      { x.printStackTrace();
      }
    }
    
  }


}
