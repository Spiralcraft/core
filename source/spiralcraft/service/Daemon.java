package spiralcraft.service;

import spiralcraft.exec.Executable;

/**
 * An executable that starts, responds to events, and terminates
 *   upon receipt of an applicable signal.
 *
 * A daemon is the outermost layer of the services framework.
 */
public class Daemon
  extends ServiceGroup
  implements Executable
{
  private Object _eventMonitor=new Object();

  public void exec(String[] args)
  {
    try
    { 
      init();
      handleEvents();
      destroy();
    }
    catch (ServiceException x)
    { x.printStackTrace();
    }
  }

  public void terminate()
  { 
    synchronized (_eventMonitor)
    { _eventMonitor.notify();
    }

  }

  private void handleEvents()
  {
    synchronized (_eventMonitor)
    { 
      try
      { _eventMonitor.wait();
      }
      catch (InterruptedException x)
      { x.printStackTrace();
      }
    }
  }

}
