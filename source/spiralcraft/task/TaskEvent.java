package spiralcraft.task;

import java.util.EventObject;

public class TaskEvent
  extends EventObject
{
  public TaskEvent(Task source)
  { super(source);
  }
}
