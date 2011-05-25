package spiralcraft.app.kit;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.State;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;

public abstract class FrameHandler
  implements MessageHandler
{

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { return focusChain;
  }

  @Override
  public final void handleMessage
    (Dispatcher dispatcher,Message message,MessageHandlerChain next)
  {
    State state=dispatcher.getState();
    if (state!=null)
    { 
      if (state.isNewFrame())
      { doHandler(dispatcher,message,next);
      }
    }
    else
    { next.handleMessage(dispatcher,message);
    }
  }
  
  protected abstract void
    doHandler
      (Dispatcher dispatcher
      ,Message message
      ,MessageHandlerChain next
      );
  
  @Override
  public Message.Type getType()
  { return null;
  }

}
