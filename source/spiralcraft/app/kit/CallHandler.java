package spiralcraft.app.kit;

import spiralcraft.app.CallContext;
import spiralcraft.app.CallMessage;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.command.CallInterface;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;

/**
 * <p>Retains a ThreadLocal reference to a Component's CallContext
 *     for retrieval by the Component 
 * </p>
 *  
 * 
 * @author mike
 *
 * @param <Tstate>
 */
public class CallHandler
  extends AbstractMessageHandler
{
  public final CallInterface callInterface
    =new CallInterface();
  
  private CallContext callContext;
      
  { type=CallMessage.TYPE;
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    callContext=LangUtil.assertInstance(CallContext.class,focusChain);
    return super.bind(focusChain);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final void doHandler
    (Dispatcher dispatcher,Message message,MessageHandlerChain next)
  {
    if (callContext.getNextSegment()==null)
    {
      CallMessage call=(CallMessage) message;
      callInterface.execute(call.call);
      log.fine("Executed "+call.call);
    }
    else
    { next.handleMessage(dispatcher,message);
    }

  }
  
}
