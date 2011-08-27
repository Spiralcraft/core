package spiralcraft.app.kit;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.State;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

/**
 * <p>Triggers the evaluation of an Expression when a message is
 *   handled.
 * </p>
 *  
 * 
 * @author mike
 *
 * @param <Tstate>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TriggerHandler<Tstate extends State>
  implements MessageHandler
{
 
  private Binding<?> x;
  private boolean post;
  private Message.Type type;
  
  public TriggerHandler(Message.Type messageType,Expression<?> x,boolean post)
  { 
    this.type=messageType;
    this.post=post;
    this.x=new Binding(x);
  }
  
  public TriggerHandler()
  {
  }

  public void setMessageType(Message.Type type)
  { this.type=type;
  }
  
  public void setPost(boolean post)
  { this.post=post;
  }
  
  public void setX(Expression<?> x)
  { this.x=new Binding(x);
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    x.bind(focusChain);
    return focusChain;
  }
  
  
  @Override
  public final void handleMessage
    (Dispatcher dispatcher,Message message,MessageHandlerChain next)
  {
    if (!post && (type==null || message.getType()==type) )
    { x.get();
    }
    next.handleMessage(dispatcher,message);
    if (post && (type==null || message.getType()==type) )
    { x.get();
    }
  }


}
