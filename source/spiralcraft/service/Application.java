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



import spiralcraft.app.CallContext;
import spiralcraft.app.CallMessage;
import spiralcraft.app.DisposeMessage;
import spiralcraft.app.InitializeMessage;
import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
import spiralcraft.app.kit.SimpleState;
import spiralcraft.app.kit.StandardDispatcher;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;

import spiralcraft.command.AbstractCommandFactory;
import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.command.CommandFactory;
import spiralcraft.command.SimpleCall;


import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.task.Scenario;
import spiralcraft.util.Path;


/**
 * <p>Root component of an application
 * </p>
 *
 */
public class Application
  extends ResourceContext
  implements Runnable
{
  protected final ClassLog log=ClassLog.getInstance(getClass());
  
  private Object _eventMonitor=new Object();
  private volatile boolean _running=true;
  private volatile boolean _stopRequested=false;
  private String[] _args;
  private Scenario<?,?> afterStart;
  
  private ShutdownHook _shutdownHook=new ShutdownHook();
  private State rootState;
  private CallContext callContext=new CallContext();
  
  { this.chainOuterContext(callContext);
  }
  
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
    throws ContextualException
  { 
    if (afterStart!=null)
    { afterStart.bind(selfFocus);
    }
    return chain;
  }
  

  @Override
  public void run() 
  {

    try
    { start();
    }
    catch (LifecycleException x)
    { throw new RuntimeException("Error starting application",x);
    }
      
    try
    {
      rootState=new SimpleState(this.getChildCount(),this.id);
      new StandardDispatcher(true,new StateFrame())
        .dispatch(InitializeMessage.INSTANCE,this,rootState,null);

      if (afterStart!=null)
      { 
        Command<?,?,?> command=afterStart.command();
        command.execute();
        if (command.getException()!=null)
        { 
          throw new RuntimeException
            ("Error running afterStart command",command.getException());
        }
      }
    
      handleEvents();

      new StandardDispatcher(true,new StateFrame())
        .dispatch(DisposeMessage.INSTANCE,this,rootState,null);

    }
    finally
    {
      try
      { stop();
      }
      catch (LifecycleException x)
      { throw new RuntimeException("Error stopping application",x);
      }
      
      finally
      {
        _running=false;
        _shutdownHook.finish();
      }
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

  public void call(Path path,CallMessage message)
  {
    callContext.pushCall(path);
    try
    {
      new StandardDispatcher(true,new StateFrame())
        .dispatch(message,this,rootState,null);
    }
    finally
    { callContext.popCall();
    }
  }
  
  public <Tcontext,Tresult> Tresult 
    call(String path,String verb,Tcontext context)
  {
    SimpleCall<Tcontext,Tresult> call
      =new SimpleCall<Tcontext,Tresult>(verb,context);
    call(Path.create(path),new CallMessage(call));
    if (call.getException()!=null)
    { throw new RuntimeException(call.getException());
    }
    return call.getResult();
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
        try
        { 
          if (_running)
          { 
            log.log(spiralcraft.log.Level.INFO,"Waiting for stop...");
            wait(10000);
            log.log(spiralcraft.log.Level.INFO,"Done waiting for stop.");
          }
        }
        catch (InterruptedException x)
        { log.log(spiralcraft.log.Level.INFO,"Timed out waiting for stop.");
        }
      }
    }
    
    public void finish()
    { 
      synchronized(this)
      { notifyAll();
      }
    }
    
  }

}
