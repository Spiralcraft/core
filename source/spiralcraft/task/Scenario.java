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

import java.net.URI;

import spiralcraft.command.AbstractCommandFactory;
import spiralcraft.command.Command;
import spiralcraft.command.CommandFactory;
import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.declare.Declarable;
import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.common.namespace.ContextualName;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.ChainableContext;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.lang.spi.SimpleChannel;
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
    ,Contextual
    ,CommandFactory<Task,Tcontext,Tresult>
    ,Declarable
{

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static final Scenario<?,?> sequential(Scenario<?,?>[] sequence)
  {
    if (sequence.length==0)
    { return null;
    }
    if (sequence.length==1)
    { return sequence[0];
    }
    return new Sequence(sequence);
  }
  
  
  protected ClassLog log=ClassLog.getInstance(getClass());
  
  protected ThreadLocalChannel<TaskCommand<Tcontext,Tresult>> commandChannel;
  protected ThreadLocalChannel<Tcontext> contextChannel; 
  
  protected ClosureFocus<?> closureFocus;
  protected boolean importContext;
  
  protected boolean debug;
  protected boolean verbose;
  protected boolean logTaskResults;
  protected boolean storeResults;
  
  protected Reflector<Tcontext> contextReflector;
  protected Reflector<Tresult> resultReflector;
  
  protected Expression<Reflector<Tresult>> resultReflectorX;
 
  protected ChainableContext outerContext;
  protected Expression<Tcontext> contextX;
  protected Channel<Tcontext> contextInitChannel;
  
  protected Binding<Boolean> whenX;
  
  protected DeclarationInfo declarationInfo;
  protected URI alias;
  protected URI contextAlias;
  
  
  
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
  
  public void setAlias(ContextualName alias)
  { this.alias=alias.getQName().toURIPath();
  }
  
  public void setContextAliasURI(URI contextAliasURI)
  { this.contextAlias=contextAliasURI;
  }

  /**
   * Provide a set of Contextuals that can provide functionality to this
   *   task and any subtasks
   *   
   * @param contextuals
   */
  public void setContextuals(Contextual[] contextuals)
  { 
    if (outerContext==null)
    { outerContext=new AbstractChainableContext();
    }
    for (Contextual contextual: contextuals)
    { outerContext.chain(contextual);
    }
  }
  
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

  /**
   * <p>A Boolean Expression bound into the command context which determines 
   *   whether or not the scenario should run.
   * </p>
   * 
   * @param whenX
   */
  public void setWhenX(Binding<Boolean> whenX)
  { this.whenX=whenX;
  }

  public Binding<Boolean> getWhenX()
  { return this.whenX;
  }
  
  @Override
  public void setDeclarationInfo(DeclarationInfo declarationInfo)
  { this.declarationInfo=declarationInfo;
  }
  
  @Override
  public DeclarationInfo getDeclarationInfo()
  { return declarationInfo;
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
 
  protected abstract Task task();
  
    
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
    throws ContextualException
  { 

  }
  

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Class<Command<Task,Tcontext,Tresult>> getCommandClass()
  { 
    Class clazz=TaskCommand.class;
    return clazz;
  }
  
  @Override
  public Reflector<Tcontext> getContextReflector()
  { return contextReflector;
  }
  
  @Override
  protected Reflector<Tresult> getResultReflector()
  { return resultReflector;
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
    
    return super.getCommandReflector();
    
  }
  
  private final Focus<?> bindOuterContext(Focus<?> focusChain)
    throws ContextualException
  {
    if (outerContext!=null)
    { return outerContext.bind(focusChain);
    }
    return focusChain;
  }
  
  /**
   * Implement Contextual.bind() by inserting self and command into Focus
   *   chain and calling bindChildren() to set up FocusChain for any contained
   *   scenarios.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Focus<?> bind(
    Focus<?> context)
    throws ContextualException
  { 
    if (declarationInfo==null)
    { declarationInfo
        =new DeclarationInfo
          (null
          ,BeanReflector.getInstance(getClass()).getTypeURI()
          ,null
          );
    }
    if (debug)
    { log.debug(getDeclarationInfo()+": Binding...");
    }
    
    try
    {
      Focus<?> focusChain=context;
  
      focusChain=bindImports(focusChain);
      
      
      closureFocus
        =new ClosureFocus
          (focusChain);
      
      focusChain=closureFocus;

      focusChain=bindOuterContext(focusChain);
      
      if (contextX!=null)
      { 
        contextInitChannel=focusChain.bind(contextX);
        if (contextReflector==null)
        { contextReflector=contextInitChannel.getReflector();
        }
        if (debug)
        { log.fine("ContextReflector is "+contextReflector);
        }
  
        // Allow imports to bind against context channel
        contextChannel
          =new ThreadLocalChannel<Tcontext>(contextReflector,true);
  
      }
  
      
      if (importContext && contextChannel!=null)
      { focusChain=focusChain.chain(contextChannel);
      }
  
      focusChain=bindContext(focusChain);
      
      
  //    if (focusChain==context)
  //    { focusChain=context.chain(context.getSubject());
  //    }
  //    focusChain.addFacet
  //      (focusChain.chain
  //        (new SimpleChannel(BeanReflector.getInstance(getClass()),this,true))
  //      );
      
      focusChain=chainContext(focusChain);
      
      bindResult(focusChain);
      
      Focus<?> exportChain=bindCommand
          (focusChain);
      
      Channel selfChannel
        =new SimpleChannel<Scenario<Tcontext,Tresult>>
          ((Reflector) reflect(),this,true);
  
      Focus selfFocus=focusChain.chain(selfChannel);
      
      if (alias!=null)
      { selfFocus.addAlias(alias);
      }
      exportChain.addFacet(selfFocus);
      
      exportChain=bindExports(exportChain);
      if (exportChain==null)
      { 
        throw new IllegalArgumentException
          ("bindExports cannot return null in "+getClass().getName());
      }
      bindChildren(exportChain);
      
      // 
      if (debug)
      { log.debug(getDeclarationInfo()+": Bound");
      }
      return context.chain(selfChannel);
      
    }
    catch (ContextualException x)
    { throw new ContextualException("Could not bind scenario",declarationInfo.getLocation(),x);
    }
      
  }

  /**
   * <p>Override to bind to the contextual Focus passed to this scenario and
   *   to publish any references to be used further down the chain. This is
   *   used for expressions that access the contextual Focus in a relative
   *   manner to establish data bindings for input and output
   * </p>
   * 
   * <p>This is called outside of the closure and any context initializer
   * </p>
   * 
   * @param importChain
   * @return
   */
  protected Focus<?> bindImports(Focus<?> importChain)
    throws ContextualException
  { return importChain;
  }
  
  /**
   * <p>Override to define a context (parameter block) 
   *   and to publish any other references to be used further down the chain. 
   * </p>
   * 
   * <p>This is called inside the closure and after any contextX initializer
   *   is bound
   * </p>
   * 
   * @param importChain
   * @return
   */
  protected Focus<?> bindContext(Focus<?> contextChain)
    throws ContextualException
  { return contextChain;
  }
  
  /**
   * <p>Override to compute the result type as a function of the context
   * </p>
   *  
   * @param contextChain
   * @throws ContextualException
   */
  protected void bindResult(Focus<?> contextChain)
    throws ContextualException
  {
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
    throws ContextualException
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
    if (outerContext!=null)
    { outerContext.push();
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
    if (outerContext!=null)
    { outerContext.pop();
    }
  }
  
  
  ClosureFocus<?>.Closure enclose()
  { return closureFocus.enclose();
  }
  
  public void setDebug(boolean debug)
  { 
    this.debug=debug;
    if (debug)
    { this.verbose=true;
    }
  }

  /**
   * Indicate that the scenario should write data to the contextual
   *   OutputStream, if any.
   *   
   * @param verbose
   */
  public void setVerbose(boolean verbose)
  { this.verbose=verbose;
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    if (debug)
    { log.log(Level.DEBUG,"Initializing");
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    if (debug)
    { log.log(Level.DEBUG,"Finalizing");
    }
  }
  
  
  @Override
  /**
   * Whether this Scenario is currently operable 
   */
  public boolean isCommandEnabled()
  { return true;
  }


  protected void bindInContext(Focus<?> focusChain)
    throws BindException
  {
  
  }
  

  private Focus<?> chainContext(Focus<?> focusChain)
    throws BindException
  {
    if (contextReflector!=null)
    { 
      // Make it easy for the bindings to get at the context
      if (contextChannel==null)
      {
        contextChannel
          =new ThreadLocalChannel<Tcontext>(contextReflector);
      }

      Focus<?> contextFocus=focusChain.chain(contextChannel);
      if (contextAlias!=null)
      { 
        // Don't make the context the default subject if it has an alias
        contextFocus.addAlias(contextAlias);
      }
      focusChain=contextFocus;
        
    }
    
    bindInContext(focusChain);
    return focusChain;
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
      )
    throws BindException
  {
    
    @SuppressWarnings("unchecked")
    Reflector<TaskCommand<Tcontext,Tresult>> reflector
      =(Reflector<TaskCommand<Tcontext,Tresult>>) getCommandReflector();
    
    commandChannel
      =new ThreadLocalChannel<TaskCommand<Tcontext,Tresult>>
        (reflector);
    focusChain.addFacet(focusChain.chain(commandChannel));
    

    if (whenX!=null)
    { whenX.bind(focusChain);
    }
    return focusChain;
  }
  
}
