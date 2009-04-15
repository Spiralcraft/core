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


import spiralcraft.common.LifecycleException;
import spiralcraft.exec.BeanArguments;
import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;

import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.command.CommandFactory;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.Level;

import spiralcraft.log.ClassLog;

import spiralcraft.log.jul.HandlerAdapter;
import spiralcraft.log.jul.RegistryLogger;

/**
 * <p>An executable that starts, responds to events, and terminates
 *   upon receipt of an applicable signal.
 * </p>
 *
 * <p>A daemon is the outermost layer of the services framework.
 * </p>
 */
public class Daemon
  extends ServiceGroup
  implements Executable,Registrant
{
  private static final ClassLog log=ClassLog.getInstance(Daemon.class);
  
  private Object _eventMonitor=new Object();
  private boolean _running=true;
  private boolean _stopRequested=false;
  private String[] _args;
  
  private ShutdownHook _shutdownHook=new ShutdownHook();
  
  private CommandFactory<Daemon,Void> _terminateCommandFactory
    =new CommandFactory<Daemon,Void>()
  {    
    public boolean isCommandEnabled()
    { return _running;
    }
    
    public Command<Daemon,Void> command()
    {
    
      return new CommandAdapter<Daemon,Void>()
      {
        @Override
        public void run()
        { 
          log.info("Received terminate Command");
          terminate();
        }
      };
    }
  };


  private Handler _logHandler=new HandlerAdapter();
  private Logger _logger;

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

  
  
  public void setLogLevel(Level level)
  { _logger.setLevel(level);
  }
  

  public final void execute(String ... args)
  {
    try
    { 
      new BeanArguments(this).process(args);
      
      start();
      handleEvents();
      stop();
      synchronized(_shutdownHook)
      { _shutdownHook.notify();
      }
    }
    catch (LifecycleException x)
    { x.printStackTrace(ExecutionContext.getInstance().out());
    }
    
  }

  public CommandFactory<Daemon,Void> getTerminateCommand()
  { return _terminateCommandFactory;
  }

  public void terminate()
  { 
    _stopRequested=true;
    synchronized (_eventMonitor)
    { _eventMonitor.notify();
    }

  }

  
  private void handleEvents()
  {
    Runtime.getRuntime().addShutdownHook(_shutdownHook);
    try
    { 
      while (_running && !_stopRequested)
      {
        synchronized (_eventMonitor)
        { _eventMonitor.wait();
        }
      }
    }
    catch (InterruptedException x)
    { x.printStackTrace();
    }
    _running=false;
    try
    { Runtime.getRuntime().removeShutdownHook(_shutdownHook);
    }
    catch (IllegalStateException x)
    {
    }
    
  }
  
  class ShutdownHook
    extends Thread
  { 
    @Override
    public void run()
    { 
      terminate();
      synchronized(this)
      { 
        log.log(spiralcraft.log.Level.INFO,"Waiting for stop...");
        try
        { 
          if (_running)
          { wait(10000);
          }
        }
        catch (InterruptedException x)
        {
        }
        log.log(spiralcraft.log.Level.INFO,"Done waiting for stop.");
      }
    }
  }

}
