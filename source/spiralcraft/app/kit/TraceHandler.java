package spiralcraft.app.kit;

import spiralcraft.app.Component;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;
import spiralcraft.util.thread.BlockTimer;

public class TraceHandler
  implements MessageHandler
{

  public static final ClassLog log
    =ClassLog.getInstance(TraceHandler.class);
  
  private Component component;
  
  @Override
  public Focus<?> bind(Focus<?> focus)
    throws ContextualException
  { 
    component=LangUtil.assertInstance(Component.class,focus);
    return focus;
  }
  
  @Override
  public void handleMessage(
    Dispatcher dispatcher,
    Message message,
    MessageHandlerChain next)
  {
    log.fine("IN: "+component.getDeclarationInfo()+": "+message);
    BlockTimer.instance().push();
    try
    {
      next.handleMessage(dispatcher,message);
    }
    finally
    { 
      log.fine("OUT: "+component.getDeclarationInfo()+": "+message
              +": "+BlockTimer.instance().elapsedTimeFormatted()
              );
      BlockTimer.instance().pop();
    }
    
  }
}

