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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationCursor;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.time.Scheduler;
import spiralcraft.util.MultiException;


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
public class Batch<I,R>
  extends Scenario
    <MultiTask<Batch<I,R>.SubTask,TaskCommand<Task,R>>
    ,TaskCommand<Task,R>
    >
{
  
  private Expression<?> source;
  
  private Channel<?> sourceChannel;
  protected IterationDecorator<?,I> decorator;
  
  private ThreadLocalChannel<I> item; 
  
  private boolean parallel;
  
  private Scheduler scheduler;
  
  private Scenario<Task,R> scenario;
  
  private ClosureFocus<?> closureFocus;
  
  /**
   * @return The expression which resolves the set of items to process
   */
  public Expression<?> getSource()
  { return source;
  }
  
  /**
   * @param source The expression which resolves the set of items to process
   */
  public void setSource(Expression<?> source)
  { this.source = source;
  }

  public void setScenario(Scenario<Task,R> scenario)
  { this.scenario=scenario;
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
   * <p>Override to handle the result on completion
   * </p>
   * 
   * 
   * @param completedCommands
   */
  protected void postResult(List<TaskCommand<Task,R>> completedCommands)
  {
    if (debug)
    { log.log(Level.TRACE,""+completedCommands);
    }
  }
  
  @Override
  protected MultiTask<SubTask,TaskCommand<Task,R>> task()
  {
   
    final List<SubTask> taskList=taskList();
    if (parallel)
    { return new ParallelTask<SubTask,TaskCommand<Task,R>>(taskList);
    }
    else
    { return new SerialTask<SubTask,TaskCommand<Task,R>>(taskList);
    }
  }
  
  private LinkedList<SubTask> taskList()
  { 
    IterationCursor<I> cursor=decorator.iterator();
    LinkedList<SubTask> taskList=new LinkedList<SubTask>();
    while (cursor.hasNext())
    { 
      cursor.next();
      SubTask task=new SubTask(cursor.getValue());
      if (scheduler!=null)
      { task.setScheduler(scheduler);
      }
      taskList.add(task);
    }
    return taskList;
  }
  
  
  @Override
  protected TaskCommand
    <MultiTask<SubTask,TaskCommand<Task,R>>
    ,TaskCommand<Task,R>
    >
    createCommand(MultiTask<SubTask,TaskCommand<Task,R>> task)
  {
    return 
      new TaskCommand
        <MultiTask<SubTask,TaskCommand<Task,R>>
        ,TaskCommand<Task,R>
        >
        (Batch.this,task)
      { 
      
      
        { collectResults=true;
        }
        
        @Override
        public void run()
        { 
          closureFocus.push();
          try
          { super.run();
          }
          finally
          { closureFocus.pop();
          }
          

          
          List<SubTask> subtasks=task.getSubtasks();
          if (debug)
          { log.log(Level.TRACE,"Launching "+subtasks.size()+" subtasks");
          }
          
          List<Exception> exceptionList=new ArrayList<Exception>();
          for (SubTask subtask: subtasks)
          { 
            TaskCommand<Task,R> completedCommand
              =subtask.getCompletedTaskCommand();
            
            if (debug)
            { log.log(Level.FINE,""+completedCommand.getResult());
            }
            
            if (completedCommand.getException()!=null)
            { exceptionList.add(completedCommand.getException());
            }
            
            if (debug)
            {
              log.log
                (ClassLog.DEBUG,"Command resulted in Exception: "
                +completedCommand.getException()
                ,completedCommand.getException()
                );
            }
          }
          if (exceptionList.size()>0)
          { 
            setException
              (new MultiException
                ("Multiple exceptions"
                ,exceptionList.toArray(new Exception[exceptionList.size()])
                )
              );
          }
          postResult(getResult());
        }
      };
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    sourceChannel=focusChain.bind(source);
    decorator=sourceChannel.decorate(IterationDecorator.class);
    if (decorator==null)
    { throw new BindException
        ("Not iterable: "+sourceChannel.getReflector().getTypeURI());
    }
    
    closureFocus=new ClosureFocus(focusChain);
    
    focusChain=closureFocus;

    item=new ThreadLocalChannel(decorator.getComponentReflector());
    focusChain=scenario.bind(focusChain.chain(item));
    
    if (parallel)
    { scheduler=new Scheduler();
    }
    return focusChain;
  }
 
  /** 
   * <p>Sets up a value (the batch item) in a ThreadLocal context and executes
   *   the configured command within that context
   * </p>
   * 
   * @author mike
   *
   */
  public class SubTask
    extends AbstractTask<TaskCommand<Task,R>>
  {
    private final I value;
    private volatile TaskCommand<Task,R> completedTaskCommand;

    public SubTask(I value)
    { this.value=value;
    }

    public TaskCommand<Task,R> getCompletedTaskCommand()
    { return completedTaskCommand;
    }

    @Override
    public void work()
    {
      item.push(value);
      try
      { 
        completedTaskCommand=scenario.command();
        completedTaskCommand.setCollectResults(true);
        try
        { completedTaskCommand.execute();
        }
        finally 
        { addResult(completedTaskCommand);
        }
      }
      finally
      { item.pop();
      }
    }

  }   




  
}
