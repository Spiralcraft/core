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
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationCursor;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
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
public class BatchScenarioLegacy<I,R>
  extends Scenario
{
  
  private Expression<?> source;
  
  private Expression<Command<?,R>> command;

  private URI targetTypeURI;
  private URI targetURI;
  
  private AbstractXmlObject<?,?> target;
  
  private Channel<?> sourceChannel;
  protected IterationDecorator<?,I> decorator;
  
  private ThreadLocalChannel<I> item; 
  
  private Channel<Command<?,R>> subCommandChannel;  
  
  private CommandDelegate delegate;
  
  private boolean parallel;
  
  private Scheduler scheduler;
  
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
  
  
  /**
   * <p>Override to handle the result on completion
   * </p>
   * 
   * 
   * @param completedCommands
   */
  protected void postResult(List<Command<?,R>> completedCommands)
  {
    log.log(Level.FINE,""+completedCommands);
  }
  
  @Override
  protected MultiTask<CommandSubTask> task()
  {
   
    final List<CommandSubTask> taskList=taskList();
    if (parallel)
    { return new ParallelTask<CommandSubTask>(taskList);
    }
    else
    { return new SerialTask<CommandSubTask>(taskList);
    }
  }
  
  private LinkedList<CommandSubTask> taskList()
  { 
    IterationCursor<I> cursor=decorator.iterator();
    LinkedList<CommandSubTask> taskList=new LinkedList<CommandSubTask>();
    while (cursor.hasNext())
    { 
      cursor.next();
      CommandSubTask task=new CommandSubTask(cursor.getValue());
      if (scheduler!=null)
      { task.setScheduler(scheduler);
      }
      taskList.add(task);
    }
    return taskList;
  }
  
  
  @Override
  protected TaskCommand
    createCommand(Task  task)
  {
    return 
      new TaskCommand
        (BatchScenarioLegacy.this,task)
      { 
        
        @SuppressWarnings("unchecked")
        @Override
        public void run()
        { 
          super.run();
          List<CommandSubTask> subtasks
            =((MultiTask<CommandSubTask>) task).getSubtasks();
          List<Command<?,R>> results
            =new ArrayList<Command<?,R>>(subtasks.size());
          for (CommandSubTask subtask: subtasks)
          { 
            Command<?,R> completedCommand=subtask.getCompletedCommand();
            log.log(Level.FINE,""+completedCommand.getResult());
            
            results.add(completedCommand);
            if (completedCommand.getException()!=null)
            { 
              log.log
                (ClassLog.WARNING,"Error: "
                +completedCommand.getException()
                ,completedCommand.getException()
                );
              completedCommand.getException().printStackTrace();
            }
          }
          setResult(results);
          postResult(results);
        }
      };
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    sourceChannel=focusChain.bind(source);
    decorator=sourceChannel.<IterationDecorator>decorate(IterationDecorator.class);
    
    item=new ThreadLocalChannel(decorator.getComponentReflector());
    target=AbstractXmlObject.activate
      (targetTypeURI,targetURI,null,focusChain.chain(item));
    
    subCommandChannel=target.getFocus().bind(command);

    Focus<?> focus=focusChain;
    
    delegate=new CommandDelegate();
    if (parallel)
    { scheduler=new Scheduler();
    }
    return focus;
  }
 
  /** 
   * <p>Sets up a value (the batch item) in a ThreadLocal context and executes
   *   the configured command within that context
   * </p>
   * 
   * @author mike
   *
   */
  public class CommandSubTask
    extends AbstractTask
  {
    private final I value;
    private volatile Command<?,R> completedCommand;

    public CommandSubTask(I value)
    { this.value=value;
    }

    public Command<?,R> getCompletedCommand()
    { return completedCommand;
    }

    @Override
    public void work()
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
        if (completedCommand!=null)
        { addResult(completedCommand);
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
      Command<?,R> command=subCommandChannel.get();
      command.execute();
      return command;
    }
  }



  
}
