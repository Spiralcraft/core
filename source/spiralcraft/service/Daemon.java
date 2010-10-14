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


import java.net.URI;

import spiralcraft.app.Component;
import spiralcraft.app.spi.AbstractComponent;
import spiralcraft.app.spi.StandardContainer;
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
import spiralcraft.vfs.context.ContextResourceMap;


/**
 * <p>An executable that starts, responds to events, and terminates
 *   upon receipt of an applicable signal.
 * </p>
 *
 * <p>A daemon is the outermost layer of the services framework.
 * </p>
 */
public class Daemon
  extends AbstractComponent
  implements Executable
{
  private static final ClassLog log=ClassLog.getInstance(Daemon.class);
  
  private Object _eventMonitor=new Object();
  private boolean _running=true;
  private boolean _stopRequested=false;
  private String[] _args;
  private Scenario<?,?> afterStart;
  private URI resourceContextURI;
  
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

  public void setServices(final Service[] services)
  {
    this.childContainer
      =new StandardContainer()
    {
      { 
        children=new Component[services.length];
        int i=0;
        for (Component service:services)
        { children[i++]=service;
        }
      }
      
      
      @Override
      protected Focus<?> bindChild(Focus<?> context,Component service) 
        throws BindException
      { 
        Focus<?> ret=super.bindChild(context,service);
        if (service instanceof Service && ret!=context)
        { context.addFacet(ret);
        }
        return ret;
      }
    };
  }

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
  
  public void setResourceContextURI(URI resourceContextURI)
  { this.resourceContextURI=resourceContextURI;
  }
  
  @Override
  public final void execute(String ... args)
    throws ExecutionException
  {
    try
    { 
      new BeanArguments(this).process(args);
      
      ContextResourceMap resourceMap
        =new ContextResourceMap()
      {{
         if (resourceContextURI!=null)
         { 
           log.fine(resourceContextURI.toString());
           putDefault(resourceContextURI);
         }
      }};
      
      resourceMap.push();
      try
      { executeInContext();
      }
      finally
      { resourceMap.pop();
      }

    }
    catch (LifecycleException x)
    { x.printStackTrace(ExecutionContext.getInstance().out());
    }
    
  }

  private void executeInContext() 
    throws ExecutionException, LifecycleException
  {
    try
    { bind(new BeanFocus<Daemon>(this));
    }
    catch (BindException x)
    { throw new ExecutionException("Error binding",x);
    }
    
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
