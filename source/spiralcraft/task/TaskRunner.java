//
// Copyright (c) 2009,2010 Michael Toth
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

import java.io.PrintStream;
import java.net.URI;

import spiralcraft.command.Command;
import spiralcraft.common.LifecycleException;
import spiralcraft.data.persist.AbstractXmlObject;

import spiralcraft.exec.BeanArguments;
import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;
import spiralcraft.exec.ExecutionException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.log.Log;
import spiralcraft.service.Service;
import spiralcraft.util.string.StringConverter;

/**
 * <p>An Executable that runs a task Scenario
 * </p>
 * 
 * @author mike
 *
 */
public class TaskRunner
  implements Executable
{

  private Scenario<?,?> scenario;
  private Scenario<?,?> chainLink;
  
  private Focus<?> rootFocus=new SimpleFocus<Void>();
  
  private final Log log=ClassLog.getInstance(TaskRunner.class);
  private Level debugLevel=Level.INFO;
  
  private Thread executeThread;

  private Thread shutdownThread=new ShutdownHook();
  private Service service;
  private URI serviceURI;
  
  
  {
    Runtime.getRuntime().addShutdownHook(shutdownThread);
  }
  
  /**
   * Log various messages during operation
   * 
   * @param debug
   */
  public void setDebugLevel(Level debugLevel)
  { this.debugLevel=debugLevel;
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
  
  @SuppressWarnings("rawtypes")
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
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
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
      }
      catch (BindException x)
      { throw new ExecutionException("Error binding service "+serviceURI,x);
      }
    }
    
    Focus<?> focus=rootFocus;
    
    if (service!=null)
    {
      // Provide access to the Service
      
      if (service instanceof Contextual)
      { 
        // Service will export into the Focus- ie. a ServiceGroup
        try
        { focus=((Contextual) service).bind(focus);
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
      
      StringConverter converter
        =scenario.getResultReflector()!=null
        ?scenario.getResultReflector().getStringConverter()
        :null;
        
      try
      { 
        Command<?,?,?> command=scenario.command();
        command.execute();
        
        Object result=command.getResult();
        if (result!=null)
        {
          PrintStream out=ExecutionContext.getInstance().out();
          out.println
            (converter!=null
            ?converter.toString(result)
            :result.toString()
            );
        }
        
        if (result!=null && debugLevel.isDebug())
        { 
          log.log
            (Level.DEBUG,"Scenario "+scenario+" completed with result: "
            +result
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
      if (debugLevel.isDebug())
      { log.log(Level.DEBUG,"Interrupting for shutdown");
      }
      if (executeThread!=null && executeThread.isAlive())
      { executeThread.interrupt();
      }
    }
  }  
}
