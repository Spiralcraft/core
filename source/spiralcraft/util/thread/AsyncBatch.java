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
package spiralcraft.util.thread;

import java.net.URI;

import java.util.LinkedList;

import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;

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
import spiralcraft.task.Task;

/**
 * <p>Runs tasks in parallel for each item in an Iterable source
 * </p>
 * 
 * @author mike
 *
 * @param <S>
 */
public class AsyncBatch<S,I,R,T>
  implements FocusChainObject
{
  
  private Expression<S> source;
  
  private Expression<Command<?,?>> operation;
  private URI targetTypeURI;
  private URI targetURI;
  
  private AbstractXmlObject<T,?> target;
  
  private Channel<S> sourceChannel;
  private IterationDecorator<S,I> decorator;

  private ThreadLocalChannel<I> item;
  private Focus<I> itemFocus;
  
  
  private Channel<Command<?,?>> operationChannel;
  
  private OperationDelegate delegate;
  
  
  @SuppressWarnings("unchecked")
  @Override
  public void bind(
    Focus<?> focusChain)
    throws BindException
  {
    sourceChannel=focusChain.bind(source);
    decorator=sourceChannel.decorate(IterationDecorator.class);
    item=new ThreadLocalChannel(decorator.getComponentReflector());
    itemFocus=focusChain.chain(item);
    target=AbstractXmlObject.<T>create
      (targetTypeURI,targetURI,null,itemFocus);
    
    operationChannel=target.getFocus().bind(operation);
    delegate=new OperationDelegate();
    
  }

  /**
   * @return the source
   */
  public Expression<S> getSource()
  {
    return source;
  }

  /**
   * @param source the source to set
   */
  public void setSource(
    Expression<S> source)
  {
    this.source = source;
  }

  /**
   * @return the operation
   */
  public Expression<Command<?, ?>> getOperation()
  {
    return operation;
  }

  /**
   * @param operation the operation to set
   */
  public void setOperation(
    Expression<Command<?, ?>> operation)
  {
    this.operation = operation;
  }

  /**
   * @return the targetTypeURI
   */
  public URI getTargetTypeURI()
  {
    return targetTypeURI;
  }

  /**
   * @param targetTypeURI the targetTypeURI to set
   */
  public void setTargetTypeURI(
    URI targetTypeURI)
  {
    this.targetTypeURI = targetTypeURI;
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

  public void execute()
  { 
    
    IterationCursor<I> cursor=decorator.iterator();
    LinkedList<Task> taskList=new LinkedList<Task>();
    while (cursor.hasNext())
    { 
      
      taskList.add(new CommandTask(cursor.getValue()));
      cursor.next();
    }
    AsyncTask task=new AsyncTask(taskList);
    task.run();
  }
  

  
  @Override
  public Focus<?> getFocus()
  { return target.getFocus();
  }

  public Command<AsyncBatch<S,I,R,T>,?> runCommand()
  {
    return new CommandAdapter<AsyncBatch<S,I,R,T>,Void>()
    { 
      public void run()
      { execute();
      }
    };
  }
  
  
  public class CommandTask
    extends AbstractTask
  {
    private final I value;
    
    public CommandTask(I value)
    { this.value=value;
    }
    
    public void execute()
    {
      item.push(value);
      try
      { 
        if (target!=null)
        {
          try
          { target.runInContext(delegate);
          }
          catch (DelegateException x)
          { throw new RuntimeException(x);
          }
        
        }
        else
        { delegate.run();
        }
      }
      finally
      { item.pop();
      }
    }
    
  }
  
  class OperationDelegate
    implements Delegate<Void>
  {

    @Override
    public Void run()
    { 
      operationChannel.get().execute();
      return null;
    }
  }
}
