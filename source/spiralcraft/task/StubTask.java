package spiralcraft.task;

/**
 * An stub implementation of a task for development/example purposes
 */
public class StubTask
  extends AbstractTask
{
  
  protected void execute()
  {
    setUnitsInTask(1);
    setOpsInUnit(100);
    setUnitsCompletedInTask(0);
    setOpsCompletedInUnit(0);
    setCurrentUnitTitle("Counting");
    
    try
    {
      for (int i=0;i<100;i++)
      { 
        Thread.currentThread().sleep(250);
        setOpsCompletedInUnit(i);
        setCurrentOpTitle(Integer.toString(i));
      }
    }
    catch (InterruptedException x)
    { x.printStackTrace();
    }
  }
  
}
