package spiralcraft.app.kit;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.Parent;
import spiralcraft.app.State;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

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
 
  private static final ClassLog log
    =ClassLog.getInstance(TriggerHandler.class);
  private Level logLevel
    =ClassLog.getInitialDebugLevel(TriggerHandler.class,Level.INFO);
  
//  private Level logLevel
//    =ClassLog.getInitialDebugLevel(TriggerHandler.class,Level.INFO);
  
  private Binding<?> x;
  private boolean post;
  private Message.Type type;
  private boolean subscribe;
  private ThreadLocalChannel<Message> message;
  
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
  
  /**
   * <p>Whether a subscription will be registered with ancestors for this 
   *   message type.
   * </p>
   *   
   * @param subscribe
   */
  public void setSubscribe(boolean subscribe)
  { this.subscribe=subscribe;
  }
  
  public void setX(Expression<?> x)
  { this.x=new Binding(x);
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    if (logLevel.isDebug())
    { log.debug("Binding TriggerHandler for type:"+type);
    }
    Focus<?> messageChain=focusChain;
    if (type!=null && type.typeClass!=null)
    { 
      message
        =new ThreadLocalChannel
          (BeanReflector.getInstance(type.typeClass)
          ,true
          ,focusChain.getSubject()
          );
      messageChain=messageChain.chain(message);
      if (logLevel.isFine())
      { log.fine("Bound message into focus chain "+type.typeClass);
      }
    }
    x.bind(messageChain);
    if (subscribe && type!=null)
    { 
      LangUtil.findInstance(Parent.class,focusChain)
        .subscribe(new Message.Type[] {type});
    }
    return focusChain;
  }
  
  public void setLogLevel(Level logLevel)
  { this.logLevel=logLevel;
  }
  
  private void eval(Message message)
  {
    if (this.message!=null)
    { this.message.push(message);
    }
    try
    { x.get();
    }
    catch (Exception x)
    { log.log(Level.WARNING,"Error evaluating trigger",x);
    }
    finally
    {
      if (this.message!=null)
      { this.message.pop();
      }
    }
  }
  
  @Override
  public final void handleMessage
    (Dispatcher dispatcher,Message message,MessageHandlerChain next)
  {
    if (!dispatcher.isTarget())
    { next.handleMessage(dispatcher,message);
    }
    else
    {
      if (!post && (type==null || message.getType()==type) )
      { eval(message);
      }
      next.handleMessage(dispatcher,message);
      if (post && (type==null || message.getType()==type) )
      { eval(message);
      }
    }
  }


}
