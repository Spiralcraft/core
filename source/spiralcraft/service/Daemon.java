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
import spiralcraft.exec.ExecutionException;

import spiralcraft.command.AbstractCommandFactory;
import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.command.CommandFactory;


import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanFocus;
import spiralcraft.log.ClassLog;
import spiralcraft.task.Scenario;


/**
 * <p>An executable that starts, responds to events, and terminates
 *   upon receipt of an applicable signal.
 * </p>
 *
 * <p>A daemon is the outermost layer of the services framework.
 * </p>
 */
public class Daemon
  extends ResourceContext
  implements Executable
{
  private static final ClassLog log=ClassLog.getInstance(Daemon.class);
  
  private Object _eventMonitor=new Object();
  private volatile boolean _running=true;
  private volatile boolean _stopRequested=false;
  private String[] _args;
  private Scenario<?,?> afterStart;
  
  private ShutdownHook _shutdownHook=new ShutdownHook();
  
  public final CommandFactory<Void,Void,Void> terminate
    =new AbstractCommandFactory<Void,Void,Void>()
  {    
    @Override
    public boolean isCommandEnabled()
    { return _running;
    }
    
    @Override
    public Command<Void,Void,Void> command()
    {
    
      return new CommandAdapter<Void,Void,Void>()
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



  public void setAfterStart(Scenario<?,?> afterStart)
  { this.afterStart=afterStart;
  }
  
  public String[] getArguments()
  { return _args;
  }  

  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws BindException
  { 
    if (afterStart!=null)
    { afterStart.bind(selfFocus);
    }
    return chain;
  }
  

  
  @Override
  public final void execute(String ... args)
    throws ExecutionException
  {
    try
    { 
      new BeanArguments(this).process(args);
      
      try
      { bind(new BeanFocus<Daemon>(this));
      }
      catch (BindException x)
      { throw new ExecutionException("Error binding",x);
      }
      
      push();
      try
      { executeInContext();
      }
      finally
      { pop();
      }

    }
    catch (LifecycleException x)
    { x.printStackTrace(ExecutionContext.getInstance().out());
    }
    
  }

  private void executeInContext() 
    throws ExecutionException, LifecycleException
  {

    
    start();
    
    if (afterStart!=null)
    { 
      Command<?,?,?> command=afterStart.command();
      command.execute();
      if (command.getException()!=null)
      { 
        throw new ExecutionException
          ("Error running afterStart command",command.getException());
      }
    }
    
    handleEvents();
    stop();
    synchronized(_shutdownHook)
    { _shutdownHook.notify();
    }
  }
  
  
  public void terminate()
  { 
    _stopRequested=true;
    synchronized (_eventMonitor)
    { 
      _eventMonitor.notifyAll();
      if (logLevel.isDebug())
      { log.debug("Notified event handler of termination...");
      }
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
        { 
          if (_running && !_stopRequested)
          { 
            if (logLevel.isDebug())
            { log.debug("Waiting for next process event...");
            }
            _eventMonitor.wait();
          }
          if (logLevel.isDebug())
          { log.debug("Event handler terminating...");
          }
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
