package spiralcraft.data.util;

import java.util.List;

import spiralcraft.command.BatchProcessor;
import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;

import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayListAggregate;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;


/**
 * <p>Runs multiple RestService invocations based on the contents of 
 *   a set of application objects and binds the result (an Aggregate of 
 *   the result-containing rest query objects) to an arbitrary part of the 
 *   FocusChain.
 * </p>
 *   
 * @author mike
 *
 */
public class DataBatchProcessor
  extends BatchProcessor<Tuple>
{

  private Expression<Aggregate<Tuple>> resultAssignment;
  private Channel<Aggregate<Tuple>> resultChannel;
  
  
  public void setResultAssignment
    (Expression<Aggregate<Tuple>> resultAssignment)
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
  public Command<BatchProcessor<Tuple>,List<Command<?,?>>> runCommand()
  {
    return new CommandAdapter<BatchProcessor<Tuple>,List<Command<?,?>>>()
    { 
      @Override
      public void run()
      { 
        setResult(runBatch());
        EditableArrayListAggregate<Tuple> result
          =new EditableArrayListAggregate<Tuple>
            (((DataReflector<Aggregate<Tuple>>) resultChannel.getReflector())
               .getType()
            );
        for (Command<?,?> command:getResult())
        { 
          Tuple resultItem=(Tuple) command.getResult();
          result.add(resultItem);
        }
        resultChannel.set(result);
      }
    };
  }  
}
