package spiralcraft.task;

import spiralcraft.common.LifecycleException;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.SimpleChannel;

public class TaskRunner
  implements Executable
{

  private Scenario<? extends Task> scenario;
  
  public void setScenario(Scenario<? extends Task> scenario)
  { this.scenario=scenario;
  }
  
  @Override
  public void execute(
    String... args)
    throws ExecutionException
  {
    try
    {
      scenario.bind
        (new SimpleFocus<TaskRunner>
          (new SimpleChannel<TaskRunner>(this,true)
          )
        );

      scenario.start();
      scenario.task().run();
      scenario.stop();
    }
    catch (BindException x)
    { throw new ExecutionException("Error binding focus",x);
    }    
    catch (LifecycleException x)
    { throw new ExecutionException("Error starting/stopping scenario",x);
    }    
  }

}
