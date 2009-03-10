package spiralcraft.task.test;

import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;

public class StubScenario
  extends Scenario<Task,Void>
{

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
        
        // TODO Auto-generated method stub
        
      }
    };    
  }

}
