package spiralcraft.log;

import spiralcraft.common.Lifecycle;

public interface EventHandler
  extends Lifecycle
{
  
  void handleEvent(Event event);

}
