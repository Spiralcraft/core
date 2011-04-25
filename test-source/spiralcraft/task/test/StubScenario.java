package spiralcraft.task.test;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;

public class StubScenario
  extends Scenario<Void,Void>
{

  protected Expression<Object> resultX;
  protected Channel<Object> resultChannel;
  
  { setLogTaskResults(true);
  }
  
  public void setResultX(Expression<Object> resultX)
  { this.resultX=resultX;
  }
  
  @Override
  protected Task task()
  {
    // TODO Auto-generated method stub
    return new AbstractTask()
    {

      @Override
      protected void work()
      { 
        log.log(Level.FINE,this+": executing");
        if (resultChannel!=null)
        { addResult(resultChannel.get());
        }
      }
    };    
  }

  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws ContextualException
  {  
    if (resultX!=null)
    { resultChannel=focusChain.bind(resultX);
    }
    super.bindChildren(focusChain);
  }
}
