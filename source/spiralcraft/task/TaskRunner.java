package spiralcraft.task;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionException;

public class TaskRunner
  implements Executable,Lifecycle
{

  private Scenario scenario;
  
  public void setScenario(Scenario scenario)
  { this.scenario=scenario;
  }
  
  @Override
  public void execute(
    String... args)
    throws ExecutionException
  {
    scenario.task().run();
  }

  @Override
  public void start()
    throws LifecycleException
  { scenario.start();
  }

  @Override
  public void stop()
    throws LifecycleException
  { scenario.stop();
  }

}
