//
// Copyright (c) 2008,2009 Michael Toth
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

import spiralcraft.command.AbstractCommandFactory;
import spiralcraft.command.Command;
import spiralcraft.command.CommandFactory;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.lang.spi.GenericReflector;
import spiralcraft.lang.spi.SimpleChannel;

import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * <p>A Scenario provides a UI independent context for Task based behavior.
 * </p>
 * 
 * <p>As a Task factory, it implements a tree of runnable operations in the
 *   form of Tasks which report progress, may generate results, and may 
 *   interact with the Scenario context via the FocusChain. 
 * </p>
 *
 * <p>Scenarios may reference and contain other Scenarios to compose 
 *   behavior in the form of the Task tree.
 * </p>
 * 
 * <p>A Scenario is a long lived object which supports concurrency. It is
 *   a CommandFactory which provides TaskCommands that run/schedule a Task
 *   instance and provide access to the result/exception.
 * </p>
 * 
 * @author mike
 */
public abstract class Scenario<Tcontext,Tresult>
  extends AbstractCommandFactory<Task,Tcontext,Tresult>
  implements Lifecycle
    ,FocusChainObject
    ,CommandFactory<Task,Tcontext,Tresult>
{

  protected ClassLog log=ClassLog.getInstance(getClass());
  
  protected ThreadLocalChannel<TaskCommand<Tcontext,Tresult>> commandChannel;
  protected ThreadLocalChannel<Tcontext> contextChannel; 
  
  protected ClosureFocus<Scenario<Tcontext,Tresult>> closureFocus;
  
  protected boolean debug;
  protected boolean verbose;
  protected boolean logTaskResults;
  protected boolean storeResults;
  
  protected Reflector<Tcontext> contextReflector;
  protected Reflector<Tresult> resultReflector;
  protected Expression<Reflector<Tresult>> resultReflectorX;
 
  protected Expression<Tcontext> contextX;
  protected Channel<Tcontext> contextInitChannel;
  
  /**
   * 
   * @return A command which runs this Scenario when invoked. Used to wire 
   *   together behavioral elements in a set of Scenarios. The Command
   *   target is the scenario, and the result is implementation specific.
   */
  @Override
  public final TaskCommand <Tcontext,Tresult>
    command()
  { 
    Task task=task();
    if (debug)
    { task.setDebug(debug);
    }
  	TaskCommand<Tcontext,Tresult> command 
  	  =createCommand
  	    (task
  	    ,contextInitChannel!=null
  	      ?contextInitChannel.get()
  	      :null
  	    );
  	return command;
  }
  
  
  protected abstract Task task();
  
  /**
   * Provide a Context expression which will be enclosed in the Focus chain
   *   for the duration of a Task and will be published for use by
   *   descendants. 
   * 
   * @param contextX
   */
  public void setContextX(Expression<Tcontext> contextX)
  { this.contextX=contextX;
  }
  
  /**
   * Define the result type of the TaskCommand that runs this Scenario
   * 
   * @param resultReflector
   */
  public void setResultReflectorX(Expression<Reflector<Tresult>> resultReflectorX)
  { this.resultReflectorX=resultReflectorX;
  }
  
  protected TaskCommand<Tcontext,Tresult> createCommand
    (Task task,Tcontext initContext)
  { 
    return new TaskCommand<Tcontext,Tresult>
      (Scenario.this,task,initContext);
  }


  public void setLogTaskResults(boolean logTaskResults)
  { this.logTaskResults=logTaskResults;
  }
  
  public void setStoreResults(boolean storeResults)
  { this.storeResults=storeResults;
  }
  
  public boolean getLogTaskResults()
  { return logTaskResults;
  }

  public boolean getStoreResults()
  { return storeResults;
  }
  
  <T> void logTaskResult(TaskEvent event,T result)
  { log.info("Task result: "+event.getSource()+" "+result.toString());
  }
  
  <T> void logTaskException(TaskEvent event,Exception x)
  { log.log(Level.WARNING,"Task threw: "+event.getSource()+" "+x,x);
  }  
  
  /**
   * <p>Publish Channels into the Focus chain for use by child Scenarios.
   * </p>
   * 
   * <p>Default implementation does nothing
   * </p>
   * 
   * <p>The supplied Focus chain already publishes the Scenario and the
   *   TaskCommand for structural and per-invocation context respectively.
   * </p>
   * 
   * 
   * @param focusChain
   * @throws BindException
   */
  protected void bindChildren(Focus<?> focusChain)
    throws BindException
  { 

  }
  

  @SuppressWarnings("unchecked")
  protected Class<TaskCommand> getCommandClass()
  { return TaskCommand.class;
  }
  
  /**
   * Override to indicate the extended type for the TaskCommand to expose
   *   custom attributes into the FocusChain expected by tasks.
   * 
   * @return
   */
  @Override
  public final Reflector<? extends Command<Task,Tcontext,Tresult>> 
    getCommandReflector()
      throws BindException
  { 
    Reflector<TaskCommand<Tcontext,Tresult>> commandReflector
      =BeanReflector.<TaskCommand<Tcontext,Tresult>>getInstance
        (getCommandClass());
    
    if (resultReflectorX!=null)
    { 
      Focus<?> rf=closureFocus;
      if (rf==null)
      { rf=new SimpleFocus<Void>(null);
      }
      resultReflector
        =rf.<Reflector<Tresult>>bind(resultReflectorX).get();
      
      if (resultReflector==null)
      { log.fine("Type expression '"+resultReflectorX+"' returned null");
      }
    }
    
    if (contextReflector!=null || resultReflector!=null)
    {
      GenericReflector<TaskCommand<Tcontext,Tresult>> gr
        =new GenericReflector<TaskCommand<Tcontext,Tresult>>
          (commandReflector.getTypeURI(),commandReflector);
      if (contextReflector!=null)
      { gr.enhance("context",null,contextReflector);
      }
      if (resultReflector!=null)
      { gr.enhance("result",null,resultReflector);
      }
      // gr.setDebug(true);
      commandReflector=gr;
    }
    return commandReflector;
  }
  
  /**
   * Implement FocusChainObject.bind() by inserting self and command into Focus
   *   chain and calling bindChildren() to set up FocusChain for any contained
   *   scenarios.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    
    if (contextX!=null)
    { 
      contextInitChannel=focusChain.bind(contextX);
      if (contextReflector==null)
      { contextReflector=contextInitChannel.getReflector();
      }
      if (debug)
      { log.fine("ContextReflector is "+contextReflector);
      }
    }
    
    focusChain=bindImports(focusChain);
    
    Channel selfChannel
      =new SimpleChannel<Scenario<Tcontext,Tresult>>
        ((Reflector) reflect(),this,true);
    
    focusChain=focusChain.chain(selfChannel);
    
    
    closureFocus
      =new ClosureFocus<Scenario<Tcontext,Tresult>>
        ((Focus<Scenario<Tcontext,Tresult>>) focusChain);
    

    
    
    Focus<?> exportChain=bindCommand
        (closureFocus
        ,(Reflector<TaskCommand<Tcontext,Tresult>>) getCommandReflector()
        );
    
    exportChain=bindExports(exportChain);
    bindChildren(exportChain);
    
    // 
    return focusChain;
      
  }

  /**
   * <p>Override to bind to the contextual Focus passed to this scenario and
   *   to publish any references to be used further down the chain. This is
   *   used for expressions that access the contextual Focus in a relative
   *   manner to establish input data paths.
   * </p>
   * 
   * <p>This is called after any context initializer is bound and before
   *   the Scenario itself or any call specific expressions are bound.
   * </p>
   * 
   * @param importChain
   * @return
   */
  protected Focus<?> bindImports(Focus<?> importChain)
    throws BindException
  { return importChain;
  }
  
  /**
   * <p>Override to bind to the Focus chain at the point where all call 
   *   specific contextual data is available and to publish any references
   *   to or establish the context for chained Scenarios. This is used to
   *   bind internal Scenario-specific functionality
   * </p>
   * 
   * 
   * @param exportChain
   * @return
   */
  protected Focus<?> bindExports(Focus<?> exportChain)
    throws BindException
  { return exportChain;
  }

  /**
   * Called from TaskCommand to start an execution instance
   *   
   * @param command
   */
  void pushCommand(TaskCommand<Tcontext,Tresult> command)
  { 
    if (commandChannel==null)
    { 
      throw new IllegalStateException
        ("Scenario.bind() never called in "+getClass().getName());
    }
    commandChannel.push(command);
    if (contextReflector!=null)
    { contextChannel.push(command.getContext());
    }
  }
  
  /**
   * Called from TaskCommand to deallocate an execution instance
   */
  void popCommand()
  { 
    commandChannel.pop();
    if (contextReflector!=null)
    { contextChannel.pop();
    }
  }
  
  
  ClosureFocus<Scenario<Tcontext,Tresult>>.Closure enclose()
  { return closureFocus.enclose();
  }
  
  public void setDebug(boolean debug)
  { 
    this.debug=debug;
    if (debug)
    { this.verbose=true;
    }
  }

  public void setVerbose(boolean verbose)
  { this.verbose=verbose;
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    if (verbose)
    { log.log(Level.INFO,"Initializing");
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    if (verbose)
    { log.log(Level.INFO,"Finalizing");
    }
  }
  
  
  @Override
  /**
   * Whether this Scenario is currently operable 
   */
  public boolean isCommandEnabled()
  { return true;
  }


  

  /**
   * Bind the ThreadLocal command channel
   * 
   * @param focusChain
   * @param clazz
   * @return
   */
  private Focus<?> 
    bindCommand
      (Focus<?> focusChain
      ,Reflector<TaskCommand<Tcontext,Tresult>> reflector
      )
  {
    commandChannel
      =new ThreadLocalChannel<TaskCommand<Tcontext,Tresult>>
        (reflector);
    focusChain=focusChain.chain(commandChannel);
    
    if (contextReflector!=null)
    { 
      // Make it easy for the bindings to get at the context
      contextChannel
        =new ThreadLocalChannel<Tcontext>(contextReflector);
      focusChain=focusChain.chain(contextChannel);
    }
    return focusChain;
  }
  
}
