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



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Date;
import java.util.Properties;

import spiralcraft.app.CallContext;
import spiralcraft.app.CallMessage;
import spiralcraft.app.DisposeMessage;
import spiralcraft.app.InitializeMessage;
import spiralcraft.app.PlaceContext;
import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
import spiralcraft.app.kit.SimpleState;
import spiralcraft.app.kit.StandardDispatcher;
import spiralcraft.common.ContextualException;
import spiralcraft.common.Disposable;
import spiralcraft.common.DisposableContext;
import spiralcraft.common.LifecycleException;
import spiralcraft.exec.ExecutionContext;
import spiralcraft.io.MuxOutputStream;
import spiralcraft.command.AbstractCommandFactory;
import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.command.CommandFactory;
import spiralcraft.command.SimpleCall;


import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.task.Scenario;
import spiralcraft.util.Path;
import spiralcraft.util.Property;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.util.string.DateToString;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;


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

  private final ServiceRegistry serviceRegistry=new ServiceRegistry();
  
  private Object _eventMonitor=new Object();
  private volatile boolean _running=true;
  private volatile boolean _stopRequested=false;
  private String[] _args;
  private Scenario<?,?> afterStart;
  
  private Disposer disposer=new Disposer();
  private State rootState;
  private CallContext callContext=new CallContext();
  private PlaceContext placeContext;
  private DateToString logFormat=new DateToString("yyyy-MM-dd HH:mm:ss.SSS");
  private MuxOutputStream out
    =new MuxOutputStream(ExecutionContext.getInstance().out(),true);
  private MuxOutputStream err
    =new MuxOutputStream(ExecutionContext.getInstance().err(),true);
  private Properties properties = new Properties();
  private URI[] propertySearchURIs=new URI[0];
  
  { 
    this.chainOuterContext(callContext);
  }
  
  public void addOutputMonitorStream(OutputStream out)
  { this.out.addMux(out);
  }
  
  public void addErrorMonitorStream(OutputStream out)
  { this.err.addMux(out);
  }

  public void setPropertySearchURIs(URI[] propertySearchURIs)
  { this.propertySearchURIs=propertySearchURIs;
  }
  
  public void setProperties(Property[] properties)
  { 
    for (Property property: properties)
    { this.properties.setProperty(property.getKey(),property.getValue());
    }
  }
  
  public String getProperty(String name)
  { return properties.getProperty(name);
  }
  
  public void log(String message)
  { 
    String line=logFormat.toString(new Date())+": "+message+"\r\n";
    try
    { out.write(line.getBytes());
    }
    catch (IOException x)
    { }
    log.log(Level.INFO,message,null,1);
  }
  
  public void logError(String message)
  { 
    String line=logFormat.toString(new Date())+": "+message+"\r\n";
    try
    { err.write(line.getBytes());
    }
    catch (IOException x)
    { }
    log.log(Level.WARNING,message,null,1);
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


  public void setPlaceContext(PlaceContext placeContext)
  { 
    this.placeContext=placeContext;
    addContext(placeContext);
  }

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
    chain.addFacet
      (chain.chain(new SimpleChannel<ServiceRegistry>(serviceRegistry,true)));
    chain=super.bindImports(chain);
    if (afterStart!=null)
    { afterStart.bind(selfFocus);
    }
    return chain;
  }
  

  @Override
  public void start()
    throws LifecycleException
  {
    loadProperties();
    if (placeContext!=null)
    { placeContext.start();
    }
    super.start();
  }
  
  @Override
  public void stop()
    throws LifecycleException
  {
    super.stop();
    if (placeContext!=null)
    { placeContext.stop();
    }
    
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
        disposer.finish();
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
  
  private void loadProperties()
  {
    for (URI uri: propertySearchURIs)
    { loadProperties(uri);
    }
  }
  
  
  private void loadProperties(URI uri)
  {
    if (!uri.isAbsolute())
    { 
      loadProperties(URIPool.create("context:/").resolve(uri));
      loadProperties(URIPool.create("context://data/").resolve(uri));
    }
    else
    { 
      try
      {
        Resource resource=Resolver.getInstance().resolve(uri);
        if (resource!=null)
        { loadProperties(resource);
        }
      }
      catch (UnresolvableURIException x)
      {
      }
      
    }
  }
  
  private void loadProperties(Resource resource)
  {
    log.fine("Looking for properties in "+resource.getURI());
    try
    {
      if (resource.exists())
      { 
        Properties newProps=new Properties(properties);
        InputStream in =resource.getInputStream();
        newProps.load(in);
        in.close();
        properties=newProps;
      }
    }
    catch (IOException x)
    { log.log(Level.WARNING,"Exception looking for properties in "+resource.getURI(),x);
    }
  }
  
  private void handleEvents()
  {
    DisposableContext.register(disposer);
    final ShutdownHook hook=new ShutdownHook(disposer);
    Runtime.getRuntime().addShutdownHook(hook);
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
    { Runtime.getRuntime().removeShutdownHook(hook);
    }
    catch (IllegalStateException x)
    {
    }
    
  }
  
  class Disposer
    implements Disposable
  {
  
    public void dispose()
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

class ShutdownHook
  extends Thread
{
  private final WeakReference<Disposable> disposer;
  
  public ShutdownHook(Disposable disposer)
  {
    this.disposer=new WeakReference<Disposable>(disposer);
  }
  
  public void run()
  {
    Disposable disposable=disposer.get();
    if (disposable!=null)
    { disposable.dispose();
    }
  }
}
  
