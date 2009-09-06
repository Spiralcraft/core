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
  
  {
    log.warning
      (getClass()+" is deprecated, and may not be available");
  }
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

  @SuppressWarnings("unchecked")
  @Override
  public void postResult(List<TaskCommand> completedCommands)
  {
    if (debug)
    {
      if (completedCommands!=null)
      {
        for (TaskCommand command:completedCommands)
        { 
          List<R> resultItems=(List<R>) command.getResult();
          for (R resultItem : resultItems)
          { log.debug("Result posted: "+resultItem);
          }
        }
      }
      else
      { log.debug("No results posted");
      }
    }
      
    if (resultChannel!=null)
    {
      EditableArrayListAggregate<R> result
      =new EditableArrayListAggregate<R>
        (((DataReflector<Aggregate<R>>) resultChannel.getReflector())
           .getType()
        );
      if (completedCommands!=null)
      {
        for (TaskCommand command:completedCommands)
        { 
          List<R> resultItems=(List<R>) command.getResult();
          for (R resultItem : resultItems)
          { result.add(resultItem);
          }
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
