package spiralcraft.app.kit;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.MessageHandler;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>Ensures that a ThreadLocal value is available when executing behavior
 *   in a component.
 * </p>
 *  
 * 
 * @author mike
 *
 * @param <Tstate>
 */
public abstract class ThreadLocalHandler<T>
  implements MessageHandler
{

  public final ThreadLocalChannel<T> reference;
  
  
  public ThreadLocalHandler(ThreadLocalChannel<T> reference)
  { this.reference=reference;
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { return focusChain;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final void handleMessage
    (Dispatcher dispatcher,Message message,MessageHandlerChain next)
  {
    reference.push(value(dispatcher));
    try
    { next.handleMessage(dispatcher,message);
    }
    finally
    { reference.pop();
    }
  }
  
  public T get()
  { return reference.get();
  }
  
  public Reflector<T> getReflector()
  { return reference.getReflector();
  }

  protected abstract T value(Dispatcher dispatcher);

}
