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

import java.util.List;

import spiralcraft.command.CommandFactory;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.ClosureFocus;
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
public abstract class Scenario
  implements Lifecycle
    ,FocusChainObject
    ,CommandFactory<Task,List<?>>
{

  protected ClassLog log=ClassLog.getInstance(getClass());
  
  protected ThreadLocalChannel<TaskCommand> commandChannel;
  
  protected ClosureFocus<Scenario> closureFocus;
  
  protected boolean debug;
  protected boolean verbose;
  protected boolean logTaskResults;
  protected boolean storeResults;
  
  
  /**
   * 
   * @return A command which runs this Scenario when invoked. Used to wire 
   *   together behavioral elements in a set of Scenarios. The Command
   *   target is the scenario, and the result is implementation specific.
   */
  public final TaskCommand 
    command()
  { 
    Task task=task();
    if (debug)
    { task.setDebug(debug);
    }
  	return createCommand(task);
  }
  
  
  protected abstract Task task();
  

  
  protected TaskCommand createCommand(Task task)
  { return new TaskCommand(Scenario.this,task);
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
  

  
  /**
   * Override to indicate the extended type for the TaskCommand to expose
   *   custom attributes into the FocusChain expected by tasks.
   * 
   * @return
   */
  public Reflector<TaskCommand> getCommandReflector()
  { return BeanReflector.<TaskCommand>getInstance(TaskCommand.class);
  }
  
  /**
   * Implement FocusChainObject.bind() by inserting self and command into Focus
   *   chain and calling bindChildren() to set up FocusChain for any contained
   *   scenarios.
   */
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 

    closureFocus
      =new ClosureFocus<Scenario>
        (focusChain.chain(new SimpleChannel<Scenario>(this,true)));
    focusChain=closureFocus;
    
    bindChildren(bindCommand(focusChain,getCommandReflector()));
    
    
    return focusChain;
      
  }

  

    
  void pushCommand(TaskCommand command)
  { 
    if (commandChannel==null)
    { 
      throw new IllegalStateException
        ("Scenario.bind() never called in "+getClass().getName());
    }
    commandChannel.push(command);
  }
  
  void popCommand()
  { commandChannel.pop();
  }
  
  
  ClosureFocus<Scenario>.Closure enclose()
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
  private Focus<TaskCommand> 
    bindCommand(Focus<?> focusChain,Reflector<TaskCommand> reflector)
  {
    commandChannel
      =new ThreadLocalChannel<TaskCommand>
        (reflector);
    
    return focusChain.chain(commandChannel);
  }
  
}
