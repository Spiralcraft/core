package spiralcraft.app.components;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.app.kit.AbstractMessageHandler;
import spiralcraft.lang.Binding;

/**
 * Evaluates an expression when a specified messageType is received
 * 
 * @author mike
 *
 */
public class OnMessage
  extends AbstractComponent
{
  private Binding<?> binding;
  private Message.Type messageType;
  
  
  
  public void setMessageType(Message.Type messageType)
  { this.messageType=messageType;
  }

  /**
   * <p>An expression to evaluate when the StateFrame changes
   * </p>
   * 
   * @param expression
   */
  public void setX(Binding<?> binding)
  {
    removeParentContextual(this.binding);
    this.binding=binding;
    addParentContextual(this.binding);
  }

  @Override
  public void addHandlers()
  {
    addHandler(new AbstractMessageHandler()
      {
        { this.type=OnMessage.this.messageType;
        }
        
        @Override
        public void doHandler(Dispatcher dispatcher,Message message,MessageHandlerChain chain)
        { binding.get();
        }
      }
    );
  }

}
