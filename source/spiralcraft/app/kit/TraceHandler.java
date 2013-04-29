package spiralcraft.app.kit;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;

public class TraceHandler
  implements MessageHandler
{

  public static final ClassLog log
    =ClassLog.getInstance(TraceHandler.class);
  
  @Override
  public Focus<?> bind(Focus<?> focus)
  { return focus;
  }
  
  @Override
  public void handleMessage(
    Dispatcher dispatcher,
    Message message,
    MessageHandlerChain next)
  {
    log.fine("IN: "+message);
    next.handleMessage(dispatcher,message);
    log.fine("OUT:"+message);
    
  }
}

