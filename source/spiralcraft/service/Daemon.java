package spiralcraft.service;

import spiralcraft.util.Arguments;

import spiralcraft.exec.Executable;

import spiralcraft.ui.Command;
import spiralcraft.ui.AbstractCommand;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.Level;

import spiralcraft.log.RegistryLogger;
import spiralcraft.log.DefaultFormatter;

/**
 * An executable that starts, responds to events, and terminates
 *   upon receipt of an applicable signal.
 *
 * A daemon is the outermost layer of the services framework.
 */
public class Daemon
  extends ServiceGroup
  implements Executable,Registrant
{
  private Object _eventMonitor=new Object();
  private boolean _running=true;
  private String[] _args;

  private Handler _logHandler=new ConsoleHandler();
  { 
    _logHandler.setFormatter(new DefaultFormatter());
    _logHandler.setLevel(Level.ALL);
  }

  private Logger _logger;

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

  public void register(RegistryNode node)
  {
    Logger logger=(Logger) node.findInstance(Logger.class);    
    if (logger==null)
    { 
      logger=new RegistryLogger();
      logger.addHandler(_logHandler);
      node.registerInstance(Logger.class,logger);
    }
    _logger=logger;
    _logger.config("Registered root logger");
  }

  public String[] getArguments()
  { return _args;
  }

  protected void processArguments()
  {
    new Arguments()
    {
      protected boolean processOption(String option)
      { 
        if (option=="verbose")
        { _logger.setLevel(Level.FINE);
        }
        else
        { return super.processOption(option);
        }
        return true;
      }

      protected boolean processArgument(String argument)
      { return super.processArgument(argument);
      }

    }.process(_args,'-');
  }

  public void execute(String[] args)
  {
    try
    { 
      _args=args;
      processArguments();
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
