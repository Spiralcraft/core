package spiralcraft.data.util;

import java.util.List;

import spiralcraft.command.BatchProcessor;
import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;

import spiralcraft.data.Aggregate;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayListAggregate;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;


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
  extends BatchProcessor<I,R>
{

  private Expression<Aggregate<R>> resultAssignment;
  private Channel<Aggregate<R>> resultChannel;
  
  
  public void setResultAssignment
    (Expression<Aggregate<R>> resultAssignment)
  { this.resultAssignment=resultAssignment;
  }
  
  
  @Override
  public void bind(
    Focus<?> focusChain)
    throws BindException
  {
    super.bind(focusChain);
    resultChannel=focus.bind(resultAssignment);
  }

  @Override
  public Command<BatchProcessor<I,R>,List<Command<?,R>>> runCommand()
  {
    return new CommandAdapter<BatchProcessor<I,R>,List<Command<?,R>>>()
    { 
      @Override
      public void run()
      { 
        setResult(runBatch());
        EditableArrayListAggregate<R> result
          =new EditableArrayListAggregate<R>
            (((DataReflector<Aggregate<R>>) resultChannel.getReflector())
               .getType()
            );
        for (Command<?,R> command:getResult())
        { 
          R resultItem=command.getResult();
          result.add(resultItem);
        }
        resultChannel.set(result);
      }
    };
  }  
}
