//
// Copyright (c) 1998,2008 Michael Toth
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.common.LifecycleException;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationCursor;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.time.Scheduler;
import spiralcraft.util.thread.Delegate;
import spiralcraft.util.thread.DelegateException;


/**
 * <p>Runs a Command for each item in an Iterable source
 * </p>
 * 
 * <p>For each item in the set referenced by the source, a Command will be
 *   run. An optional thread-safe command target can be
 *   specified, if the command target depends on having the set item visible
 *   to its Focus chain. 
 * </p>
 * 
 * <p>The item is made available in a ThreadLocal channel published in the
 *   Focus chain, where it can be used by the thread-safe target object and by
 *   the specified command. 
 * </p>
 * 
 * <p>Results are made available via the completed Command objects.
 * </p>
 * 
 * @author mike
 *
 * @param <I> The batched item type
 * @param <R> The result item type
 */
public class BatchScenario<I,R>
  implements Scenario
{
  private static final ClassLog log
    =ClassLog.getInstance(BatchScenario.class);
  
  protected Focus<?> focus;
  private Expression<?> source;
  private Expression<Command<?,R>> command;
  private URI targetTypeURI;
  private URI targetURI;
  
  private AbstractXmlObject<?,?> target;
  
  private Channel<?> sourceChannel;
  protected IterationDecorator<?,I> decorator;
  
  private ThreadLocalChannel<I> item; 
  
  private Channel<Command<?,R>> commandChannel;  
  
  private CommandDelegate delegate;
  
  private boolean parallel;
  
  private Scheduler scheduler;
  
  @Override
  public Focus<?> getFocus()
  { return focus;
  }
  
  /**
   * @return The expression which resolves the set of items to process
   */
  public Expression<?> getSource()
  { return source;
  }
  
  /**
   * @param source The expression which resolves the set of items to process
   */
  public void setSource(
    Expression<?> source)
  { this.source = source;
  }

  /**
   * <p>Whether the items of the batch should be run asynchronously in parallel
   * </p>
   * @param parallel
   */
  public void setParallel(boolean parallel)
  { this.parallel=parallel;
  }
  
  /**
   * @return The expression which resolves the Command object
   */
  public Expression<Command<?,R>> getCommand()
  { return command;
  }

  /**
   * @param The expression which resolves the Command object
   */
  public void setCommand(
    Expression<Command<?,R>> command)
  { this.command = command;
  }

  /**
   * @return the targetTypeURI
   */
  public URI getTargetTypeURI()
  { return targetTypeURI;
  }

  /**
   * @param targetTypeURI the targetTypeURI to set
   */
  public void setTargetTypeURI(
    URI targetTypeURI)
  { this.targetTypeURI = targetTypeURI;
  }

  /**
   * @return the targetURI
   */
  public URI getTargetURI()
  {
    return targetURI;
  }

  /**
   * @param targetURI the targetURI to set
   */
  public void setTargetURI(
    URI targetURI)
  {
    this.targetURI = targetURI;
  }
  
  public List<Command<?,R>> runBatch()
  { 
    if (parallel)
    { return runBatchParallel();
    }
    else
    { return runBatchSeries();
    }
  }

  
  private List<Command<?,R>> runBatchSeries()
  { 
    
    IterationCursor<I> cursor=decorator.iterator();
    
    LinkedList<Command<?,R>> results
      =new LinkedList<Command<?,R>>();
    
    while (cursor.hasNext())
    { 
      cursor.next();
      CommandTask task=new CommandTask(cursor.getValue());
      task.run();
      results.add(task.getCompletedCommand());
    }
    return results;    
    
  }
  
  /**
   * <p>Override to handle the result on completion
   * </p>
   * 
   * 
   * @param completedCommands
   */
  protected void postResult(List<Command<?,R>> completedCommands)
  {
    log.fine(""+completedCommands);
  }
  
  public Task task()
  {
   
    if (parallel)
    {
      final List<CommandTask> taskList=taskList();
      return new ParallelTask(taskList)
      {
        @Override
        public void execute()
        { 
          super.execute();
          postResult(completedCommands(taskList));
        }
      };
    }
    else
    {
      return new AbstractTask()
      {
        @Override
        public void execute()
        { postResult(runBatchSeries());
        }
        
      };
    }
  }
  
  private LinkedList<CommandTask> taskList()
  { 
    IterationCursor<I> cursor=decorator.iterator();
    LinkedList<CommandTask> taskList=new LinkedList<CommandTask>();
    while (cursor.hasNext())
    { 
      cursor.next();
      CommandTask task=new CommandTask(cursor.getValue());
      if (scheduler!=null)
      { task.setScheduler(scheduler);
      }
      taskList.add(task);
    }
    return taskList;
  }
  
  private List<Command<?,R>> completedCommands(List<CommandTask> taskList)
  {
    ArrayList<Command<?,R>> results
    =new ArrayList<Command<?,R>>(taskList.size());
    for (CommandTask task: taskList)
    { 
      Command<?,R> completedCommand=task.getCompletedCommand();
      results.add(completedCommand);
      log.fine(""+completedCommand.getResult());
      if (completedCommand.getException()!=null)
      { 
        log.log
          (ClassLog.FINE,"Error: "
            +completedCommand.getException()
          ,completedCommand.getException()
          );
        completedCommand.getException().printStackTrace();
      }
    }
    return results;      
  }

  private List<Command<?,R>> runBatchParallel()
  { 
    
    LinkedList<CommandTask> taskList=taskList();

    ParallelTask batchTask=new ParallelTask(taskList);
    batchTask.run();
    return completedCommands(taskList);

  }  
  
  public Command<BatchScenario<I,R>,List<Command<?,R>>> runCommand()
  {
    return new CommandAdapter<BatchScenario<I,R>,List<Command<?,R>>>()
    { 
      @Override
      public void run()
      { 
        setResult(runBatch());
        postResult(getResult());
      }
    };
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void bind(
    Focus<?> focusChain)
    throws BindException
  {
    sourceChannel=focusChain.bind(source);
    decorator=sourceChannel.decorate(IterationDecorator.class);
    
    item=new ThreadLocalChannel(decorator.getComponentReflector());
    target=AbstractXmlObject.create
      (targetTypeURI,targetURI,null,focusChain.chain(item));
    
    commandChannel=target.getFocus().bind(command);

    this.focus=focusChain;
    
    delegate=new CommandDelegate();
    if (parallel)
    { scheduler=new Scheduler();
    }
  }
  
  public class CommandTask
    extends AbstractTask
  {
    private final I value;
    private volatile Command<?,R> completedCommand;
    
    public CommandTask(I value)
    { this.value=value;
    }
    
    public Command<?,R> getCompletedCommand()
    { return completedCommand;
    }
    
    @Override
    public void execute()
    {
      item.push(value);
      try
      { 
        if (target!=null)
        {
          try
          { completedCommand=target.runInContext(delegate);
          }
          catch (DelegateException x)
          { throw new RuntimeException(x);
          }
        
        }
        else
        { completedCommand=delegate.run();
        }
      }
      finally
      { item.pop();
      }
    }
    
  }  
  
  class CommandDelegate
    implements Delegate<Command<?,R>>
  {

    @Override
    public Command<?,R> run()
    { 
      Command<?,R> command=commandChannel.get();
      command.execute();
      return command;
    }
  }

  @Override
  public void start()
    throws LifecycleException
  {
    // TODO: start the sub-scenario
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    // TODO: stop the sub-scenario
    
  }
  
}
