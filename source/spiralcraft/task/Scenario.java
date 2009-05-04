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
public class Scenario
  implements Lifecycle
    ,FocusChainObject
    ,CommandFactory<Scenario,List<?>>
{

  protected ClassLog log=ClassLog.getInstance(getClass());
  
  protected ThreadLocalChannel<TaskCommand> commandChannel;
  
  protected boolean debug;
  protected boolean verbose;
  protected boolean logTaskResults;
  protected boolean storeResults;
  protected Scenario chain;
  
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
  
  protected Task task()
  { return new ChainTask();
  }
  
  protected class ChainTask
    extends AbstractTask
  {

    @Override
    protected void work()
      throws InterruptedException
    { 
      if (chain!=null && exception==null)
      {
        TaskCommand command=chain.command();
        command.setCollectResults(true);
        command.execute();
        if (command.getException()!=null)
        { addException(command.getException());
        }
        addResult(command);
      }
    }
  }
  
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
   * <p>Default implementation binds the rest of the chain. When overriding,
   *   call this method with the Focus that should be published to the rest
   *   of the Chain.
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
    if (chain!=null)
    { chain.bind(focusChain);
    }
  }
  

  
  /**
   * Override to indicate the extended type for the TaskCommand to expose
   *   custom attributes into the FocusChain expected by tasks.
   * 
   * @return
   */
  protected Class<? extends TaskCommand> getCommandType()
  { return TaskCommand.class;
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
    focusChain=focusChain.chain
      (new SimpleChannel<Scenario>(this,true));
    
    bindChildren(bindCommand(focusChain,getCommandType()));
    return focusChain;
      
  }

  public void chain(Scenario chain)
  { this.chain=chain;
  }
  
  public void setChain(Scenario[] chain)
  {
    Scenario last=this;
    for (Scenario scenario:chain)
    { 
      last.chain(scenario);
      last=scenario;
    }
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
    if (chain!=null)
    { chain.start();
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  { 
    if (chain!=null)
    { chain.stop();
    }
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
    bindCommand(Focus<?> focusChain,Class<? extends TaskCommand> clazz)
  {
    commandChannel
      =new ThreadLocalChannel<TaskCommand>
        (BeanReflector.<TaskCommand>getInstance(clazz));
    return focusChain.chain(commandChannel);
  }
}
