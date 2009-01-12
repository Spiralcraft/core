package spiralcraft.task.test;

import spiralcraft.command.Command;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;

public class StubScenario
  implements Scenario
{

  private static final ClassLog log
    =ClassLog.getInstance(StubScenario.class);
  
  @Override
  public Command<? extends Scenario, ?> runCommand()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Task task()
  {
    // TODO Auto-generated method stub
    return new AbstractTask()
    {

      @Override
      protected void execute()
      { 
        log.fine(this+": executing");
        
        // TODO Auto-generated method stub
        
      }
    };
    
    
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { return focusChain;

  }


  @Override
  public void start()
    throws LifecycleException
  {

    log.fine(this+": starting");
    // TODO Auto-generated method stub
    
  }

  @Override
  public void stop()
    throws LifecycleException
  {

    log.fine(this+": stopping");
    // TODO Auto-generated method stub
    
  }

}
