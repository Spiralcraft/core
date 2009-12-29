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
//import spiralcraft.lang.spi.ClosureFocus;
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
  extends Chain<Void,List<TaskCommand<I,R>>>
    
{
  
  private Expression<?> source;
  
  private Channel<?> sourceChannel;
  protected IterationDecorator<?,I> decorator;
  
  private ThreadLocalChannel<I> item; 
  
  private boolean parallel;
  
  private Scheduler scheduler;
  
//  private ClosureFocus<?> itemClosureFocus;
  
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
  protected void postResult(List<TaskCommand<I,R>> completedCommands)
  {
    if (debug)
    { log.log(Level.TRACE,""+completedCommands);
    }
  }
  
  @Override
  protected MultiTask<SubTask> task()
  {
   
    final List<SubTask> taskList=taskList();
    if (parallel)
    { return new ParallelTask<SubTask>(taskList);
    }
    else
    { return new SerialTask<SubTask>(taskList);
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
  protected TaskCommand<Void,List<TaskCommand<I,R>>>
    createCommand(Task task,Void initContext)
  {
    return 
      new TaskCommand<Void,List<TaskCommand<I,R>>>
        (Batch.this,task,null)
      { 
      
      
        { collectResults=true;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected void onTaskCompletion()
        {
          if (chain!=null && getException()!=null)
          { 
            TaskCommand<I,R> command=((Chain<I,R>) chain).command();
            command.execute();
            (getResult()).add(command);
            if (command.getException()!=null)
            { setException(command.getException());
            }
          }          
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void run()
        { 
          super.run();
//          itemClosureFocus.push();
//          try
//          { 
            
//            super.run();

//          }
//          finally
//          { // itemClosureFocus.pop();
//          }
          

          
          List<SubTask> subtasks=((MultiTask) task).getSubtasks();
          if (debug)
          { log.log(Level.TRACE,"Launching "+subtasks.size()+" subtasks");
          }
          
          List<Exception> exceptionList=new ArrayList<Exception>();
          for (SubTask subtask: subtasks)
          { 
            TaskCommand completedCommand
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
  public void bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    sourceChannel=focusChain.bind(source);
    decorator=sourceChannel.<IterationDecorator>decorate(IterationDecorator.class);
    if (decorator==null)
    { throw new BindException
        ("Not iterable: "+sourceChannel.getReflector().getTypeURI());
    }
    
//    itemClosureFocus=new ClosureFocus(focusChain);
    
//    focusChain=itemClosureFocus;

    item=new ThreadLocalChannel(decorator.getComponentReflector());
    focusChain=focusChain.chain(item);
    
    super.bindChildren(focusChain);
    if (parallel)
    { scheduler=new Scheduler();
    }
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
    extends ChainTask
  {
    private final I value;
    private volatile TaskCommand<I,R> completedTaskCommand;

    public SubTask(I value)
    { this.value=value;
    }

    public TaskCommand<I,R> getCompletedTaskCommand()
    { return completedTaskCommand;
    }

    @Override
    public void work()
      throws InterruptedException
    {
      item.push(value);
      try
      { super.work();
      }
      finally
      { item.pop();
      }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void addResult(Object command)
    { 
      completedTaskCommand=(TaskCommand<I,R>) command;
      super.addResult(command);
    }

  }   




  
}
