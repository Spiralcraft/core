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
package spiralcraft.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.IterationCursor;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.AsyncTask;
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
 */
public class BatchProcessor<I>
  implements FocusChainObject
{
  protected Focus<?> focus;
  private Expression<?> source;
  private Expression<Command<?,?>> command;
  private URI targetTypeURI;
  private URI targetURI;
  
  private AbstractXmlObject<?,?> target;
  
  private Channel<?> sourceChannel;
  protected IterationDecorator<?,I> decorator;
  
  private ThreadLocalChannel<I> item; 
  
  private Channel<Command<?,?>> commandChannel;  
  
  private CommandDelegate delegate;
  
  private boolean parallel;

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
  public Expression<Command<?,?>> getCommand()
  { return command;
  }

  /**
   * @param The expression which resolves the Command object
   */
  public void setCommand(
    Expression<Command<?,?>> command)
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
  
  public List<Command<?,?>> runBatch()
  { 
    if (parallel)
    { return runBatchParallel();
    }
    else
    { return runBatchSeries();
    }
  }

  
  private List<Command<?,?>> runBatchSeries()
  { 
    
    IterationCursor<I> cursor=decorator.iterator();
    
    LinkedList<Command<?,?>> results
      =new LinkedList<Command<?,?>>();
    
    while (cursor.hasNext())
    { 
      CommandTask task=new CommandTask(cursor.getValue());
      task.run();
      results.add(task.getCompletedCommand());
      cursor.next();
    }
    return results;    
    
  }
  
  private List<Command<?,?>> runBatchParallel()
  { 
    
    IterationCursor<I> cursor=decorator.iterator();
    LinkedList<CommandTask> taskList=new LinkedList<CommandTask>();
    while (cursor.hasNext())
    { 
      
      taskList.add(new CommandTask(cursor.getValue()));
      cursor.next();
    }
    
    AsyncTask batchTask=new AsyncTask(taskList);
    batchTask.run();
    
    ArrayList<Command<?,?>> results
      =new ArrayList<Command<?,?>>(taskList.size());
    for (CommandTask task: taskList)
    { results.add(task.getCompletedCommand());
    }
    return results;    
  }  
  
  public Command<BatchProcessor<I>,List<Command<?,?>>> runCommand()
  {
    return new CommandAdapter<BatchProcessor<I>,List<Command<?,?>>>()
    { 
      @Override
      public void run()
      { setResult(runBatch());
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
  }
  
  public class CommandTask
    extends AbstractTask
  {
    private final I value;
    private Command<?,?> completedCommand;
    
    public CommandTask(I value)
    { this.value=value;
    }
    
    public Command<?,?> getCompletedCommand()
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
    implements Delegate<Command<?,?>>
  {

    @Override
    public Command<?,?> run()
    { 
      Command<?,?> command=commandChannel.get();
      command.execute();
      return command;
    }
  }
  
}
