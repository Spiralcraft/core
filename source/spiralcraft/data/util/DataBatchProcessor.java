package spiralcraft.data.util;

import java.util.List;


import spiralcraft.data.Aggregate;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayListAggregate;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.task.Batch;
import spiralcraft.task.Task;
import spiralcraft.task.TaskCommand;


/**
 * <p>Runs multiple invocations of a Command based on the contents of 
 *   a set of application data objects and binds the result (an Aggregate of 
 *   the individual command results) to an arbitrary part of the 
 *   FocusChain.
 * </p>
 *   
 * @author mike
 *
 */
public class DataBatchProcessor<I,R>
  extends Batch<I,R>
{

  private Expression<Aggregate<R>> resultAssignment;
  private Channel<Aggregate<R>> resultChannel;
  
  
  public void setResultAssignment
    (Expression<Aggregate<R>> resultAssignment)
  { this.resultAssignment=resultAssignment;
  }
  
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    Focus<?> focus=super.bind(focusChain);
    if (resultAssignment!=null)
    { resultChannel=focus.bind(resultAssignment);
    }
    return focus;
  }

  @Override
  public void postResult(List<TaskCommand<Task,R>> completedCommands)
  {
    if (debug)
    {
      for (TaskCommand<Task,R> command:completedCommands)
      { 
        List<R> resultItems=command.getResult();
        for (R resultItem : resultItems)
        { log.debug("Result posted: "+resultItem);
        }
      }
    }
      
    if (resultChannel!=null)
    {
      EditableArrayListAggregate<R> result
      =new EditableArrayListAggregate<R>
        (((DataReflector<Aggregate<R>>) resultChannel.getReflector())
           .getType()
        );
      for (TaskCommand<Task,R> command:completedCommands)
      { 
        List<R> resultItems=command.getResult();
        for (R resultItem : resultItems)
        { result.add(resultItem);
        }
      }
      resultChannel.set(result);
    }
    else
    {
      log.info
        ("No resultAssigment expression specified. Discarding results from "
          +completedCommands.size()+" completed subtasks."
        );
    }
  }
    
}
