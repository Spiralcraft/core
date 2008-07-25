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
package spiralcraft.service;

import spiralcraft.util.Arguments;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;

import spiralcraft.ui.Command;
import spiralcraft.ui.AbstractCommand;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.lang.reflect.Field;

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
    Logger logger=node.findInstance(Logger.class);    
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

  protected DaemonArguments newArgumentProcessor()
  { return new DaemonArguments();
  }
  
  private void processArguments()
  { newArgumentProcessor().process(_args,'-');
  }
  
  protected class DaemonArguments
    extends Arguments
  {
    protected boolean processOption(String option)
    { 
      if (option=="logLevel")
      { 
        String level=nextArgument();
        try
        {
          Field field=Level.class.getField(level);
          if (field!=null)
          { _logger.setLevel((Level) field.get(null));
          }
          else
          { throw new IllegalArgumentException("Unknown log level '"+level+"'");
          }
        }
        catch (Exception x)
        { throw new IllegalArgumentException("Unknown log level '"+level+"'");
        }
      }
      else
      { return super.processOption(option);
      }
      return true;
    }

    protected boolean processArgument(String argument)
    { return super.processArgument(argument);
    }

  }

  public final void execute(ExecutionContext context,String[] args)
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
