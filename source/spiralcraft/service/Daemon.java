package spiralcraft.service;

import spiralcraft.exec.Executable;

import spiralcraft.ui.Command;
import spiralcraft.ui.AbstractCommand;

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
  private boolean _running=true;
  private String[] _args;

  private Command _terminateCommand
    =new AbstractCommand()
  { 
    public boolean isEnabled()
    { return _running;
    }

    public void execute()
    { terminate();
    }
  };

  public String[] getArguments()
  { return _args;
  }

  public void execute(String[] args)
  {
    try
    { 
      _args=args;
      init(this);
      handleEvents();
      destroy();
    }
    catch (ServiceException x)
    { x.printStackTrace();
    }
  }

  public Command getTerminateCommand()
  { return _terminateCommand;
  }

  public void terminate()
  { 
    _running=false;
    synchronized (_eventMonitor)
    { _eventMonitor.notify();
    }

  }

  private void handleEvents()
  {
    try
    { 
      while (_running)
      {
        synchronized (_eventMonitor)
        { _eventMonitor.wait();
        }
      }
    }
    catch (InterruptedException x)
    { x.printStackTrace();
    }
    
  }

}
