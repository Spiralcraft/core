package spiralcraft.task;

public interface TaskListener
{
 
  public void taskStarted(TaskEvent event);
  
  public void taskCompleted(TaskEvent event);
}
