package spiralcraft.app.test;

import spiralcraft.app.kit.SimpleState;

public class TimerState
  extends SimpleState
{

  long startTime;
  long stopTime;
  int startCount;
  long lastRunTime;
  boolean running;
  
  public TimerState(int childCount,String componentId)
  { super(childCount, componentId);
  }

  
}
