//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.data.task;

import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.data.Aggregate;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayListAggregate;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.task.TaskCommand;


/**
 * <p>Manages the lifecycle of an Aggregate that collects data output by 
 *   subtasks.
 * </p>
 * 
 * <p>Publishes an Aggregate of the specified type in the FocusChain, where
 *   it can be retrieved and populated and/or accessed by subtasks.
 * </p>
 * 
 * <p>Creates an instance of the Aggregate per-execution, unless append=true
 *   which specifies that the Aggregate should be re-used.
 * </p>
 *   
 * <p>If append=true, the target will be read to obtain an existing Aggregate.
 *   If no existing Aggregate is found, one will be created.
 * </p>
 * 
 * @author mike
 *
 */
public class Collector<Titem>
  extends Scenario<Task,Aggregate<Titem>>
{

  private Expression<Aggregate<Titem>> targetX;
  private Channel<Aggregate<Titem>> resultChannel;
  private Type<EditableAggregate<Titem>> type;
  private boolean append;
  private ThreadLocalChannel<EditableAggregate<Titem>> aggregateChannel;
  private Scenario<?,?> scenario;
  
  /**
   * The target of this Collector, which must be of type Aggregate<?>
   * 
   * @param resultAssignment
   */
  public void setX
    (Expression<Aggregate<Titem>> targetX)
  { this.targetX=targetX;
  }

  /**
   * Indicate that existing data from the target channel should be preserved.
   *
   * If set to false (the default), the aggregate will be empty at the
   *   start of the Task.
   * 
   * @param append
   */
  public void setAppend(boolean append)
  { this.append=append;
  }
  
  public void setScenario(Scenario<?,?> scenario)
  { this.scenario=scenario;
  }
  
  /**
   * Command that is referenceable from subtasks to add an item.
   * 
   * @param item
   * @return
   */
  public Command<Collector<Titem>,Void> commandAdd(final Titem item)
  { 
    if (debug)
    { log.debug("Returning command "+item);
    }
    
    return new CommandAdapter<Collector<Titem>,Void>()
    {
      @Override
      protected void run()
      { 
        if (debug)
        { log.debug("Adding "+item);
        }
        aggregateChannel.get().add(item);
      } 
    };
  }

  @SuppressWarnings("unchecked") // Type query
  @Override
  public Focus<?> bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    Focus<?> focus=super.bindChildren(focusChain);
    if (targetX!=null)
    { 
      resultChannel=focus.bind(targetX);
    }
    if (type==null && resultChannel!=null)
    { type=((DataReflector) resultChannel.getReflector()).getType();
    }
    
    aggregateChannel
      =new ThreadLocalChannel<EditableAggregate<Titem>>
        (DataReflector.<EditableAggregate<Titem>>getInstance(type)
        ,true
        );
    
    return scenario.bind(focus.chain(aggregateChannel));
  }

  
  @Override
  protected Task task()
  {
    return new AbstractTask<Aggregate<Titem>>()
    {
        
      @Override
      public void work()
      {
        
        EditableAggregate<Titem> result=null;
        
        if (append)
        { result=(EditableAggregate<Titem>) resultChannel.get();
        }
        
        if (result==null)
        { 
          result=new EditableArrayListAggregate<Titem>(type);
          resultChannel.set(result);
        }
        
        aggregateChannel.push(result);
        try
        {
        
          TaskCommand<?,?> command
            =scenario.command();
          if (debug)
          { log.fine("Executing "+command);
          }
          command.execute();
          addResult(aggregateChannel.get());
          if (command.getException()!=null)
          { addException(command.getException());
          }
        }
        finally
        { aggregateChannel.pop();
        }
      }
    };
  }
  
}
