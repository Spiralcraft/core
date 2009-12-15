//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.task;

import java.net.URI;

import spiralcraft.command.Command;
import spiralcraft.common.LifecycleException;
import spiralcraft.data.persist.AbstractXmlObject;

import spiralcraft.exec.BeanArguments;
import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.log.Log;
import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;
import spiralcraft.service.Service;

public class TaskRunner
  implements Executable,Registrant
{

  private Scenario<?,?> scenario;
  private Scenario<?,?> chainLink;
  
  private Focus<?> rootFocus=new SimpleFocus<Void>();
  
  private final Log log=ClassLog.getInstance(TaskRunner.class);
  
  private Thread executeThread;

  private Thread shutdownThread=new ShutdownHook();
  private Service service;
  private URI serviceURI;
  private RegistryNode registryNode;
  
  
  {
    Runtime.getRuntime().addShutdownHook(shutdownThread);
  }
  
  /**
   * A Service (or ServiceGroup) to activate before running the Scenario.
   * 
   * @param service
   */
  public void setService(Service service)
  { this.service=service;
  }
  
  /**
   * Specific the URI of the resource that defines the Service or ServiceGroup
   *   that provides a context for Scenario execution.
   * 
   * @param serviceURI
   */
  public void setServiceURI(URI serviceURI)
  { this.serviceURI=serviceURI;
  }
  
  @SuppressWarnings("unchecked")
  public void setChain(Scenario<?,?>[] chain)
  { 

    Chain<?,?> chainScenario=new Chain();
    chainScenario.setChain(chain);
    scenario=chainScenario;
    chainLink=chain[chain.length-1];
  
  }
  
  /**
   * Provide a specific Focus chain into which the Scenario will be bound.
   * 
   * @param focus
   */
  public void setRootFocus(Focus<?> focus)
  { this.rootFocus=focus;
  }
  
  @Override
  public void register(RegistryNode registryNode)
  { 
    this.registryNode=registryNode;
    if (service!=null)
    { service.register(registryNode);
    }
    
  }
  
  
  public void loadScenario(URI uri)
    throws BindException
  { 
    Scenario<?,?> scenario=AbstractXmlObject.<Scenario<?,?>>create(uri,null).get();
    if (chainLink!=null)
    { 
      if (chainLink instanceof Chain<?,?>)
      { ((Chain<?,?>) chainLink).chain(scenario);
      }
      else
      { throw new BindException(chainLink+" cannot chain another scenario");
      }
    }
    chainLink=scenario;
    if (this.scenario==null)
    { this.scenario=chainLink;
    }
  }
    
  @Override
  public void execute(
    String... args)
    throws ExecutionException
  { 
    processArguments(args);    
    execute();
  }

  protected void processArguments(String[] args)
  {
    if (chainLink!=null)
    { configureScenario(args);
    }
    else
    { 
      new BeanArguments(this)
      {
        @Override
        public boolean processArgument(String arg)
        {
          URI scenarioURI=URI.create(arg);
          try
          { 
            loadScenario(scenarioURI);
            configureScenario(remainingArguments());
          }
          catch (BindException x)
          { throw new RuntimeException("Error loading scenario "+scenarioURI,x);
          }
          return true;          
        }
        
      }.process(args);
    }
  }
  
  protected void configureScenario(String[] args)
  { 
    new BeanArguments(chainLink)
    {
      @Override
      public boolean processArgument(String arg)
      {
        URI scenarioURI=URI.create(arg);
        try
        { 
          loadScenario(scenarioURI);
          configureScenario(remainingArguments());
        }
        catch (BindException x)
        { throw new RuntimeException("Error loading scenario "+scenarioURI,x);
        }
        return true;
      }
      
    }.process(args);
    
  }
  
  protected void execute()
    throws ExecutionException
  {
    executeThread=Thread.currentThread();
      
    if (scenario==null)
    { throw new ExecutionException("No scenario provided");
    }

    if (serviceURI!=null)
    { 
      try
      { 
        service=AbstractXmlObject.<Service>create(null,serviceURI).get();
        service.register(registryNode);
      }
      catch (BindException x)
      { throw new ExecutionException("Error binding service "+serviceURI,x);
      }
    }
    
    Focus<?> focus=rootFocus;
    
    if (service!=null)
    {
      // Provide access to the Service
      
      if (service instanceof FocusChainObject)
      { 
        // Service will export into the Focus- ie. a ServiceGroup
        try
        { focus=((FocusChainObject) service).bind(focus);
        }
        catch (BindException x)
        { 
          throw new ExecutionException
            ("Error binding service into Focus chain",x);
        }
      }
      else
      { 
        // Service is simply exposed into the FocusChain
        focus=focus.<Service>chain(new SimpleChannel<Service>(service,true));
      }
    }
    
    try
    {
      if (service!=null)
      { service.start();
      }
            
      scenario.bind
        (focus.chain
          (new SimpleChannel<TaskRunner>(this,true)
          )
        );


      
      scenario.start();
      try
      { 
        Command<?,?,?> command=scenario.command();
        command.execute();
        if (command.getResult()!=null)
        { 
          log.log
            (Level.INFO,"Scenario "+scenario+" completed with result: "
            +command.getResult()
            );
        }
        if (command.getException()!=null)
        {
          log.log
            (Level.SEVERE,"Scenario "+scenario+" complete with exception."
            ,command.getException()
            );
        }
      }
      catch (Throwable x)
      { log.log(Level.SEVERE,"Uncaught exception running "+scenario,x);
      }
      scenario.stop();
      
      if (service!=null)
      { service.stop();
      }
    }
    catch (BindException x)
    { throw new ExecutionException("Error binding focus",x);
    }    
    catch (LifecycleException x)
    { 
      throw new ExecutionException
        ("Error starting/stopping scenario or service",x);
    }    
  }
 
  
  class ShutdownHook
    extends Thread
  { 
    @Override
    public void run()
    { 
      log.log(Level.INFO,"Interrupting for shutdown");
      if (executeThread!=null && executeThread.isAlive())
      { executeThread.interrupt();
      }
    }
  }  
}
