package spiralcraft.app.kit;

import spiralcraft.app.Component;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.State;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>Retains a ThreadLocal reference to a Component's active State
 *     for retrieval by the Component during actions invoked by
 *     downstream components.
 * </p>
 *  
 * 
 * @author mike
 *
 * @param <Tstate>
 */
public class StateReferenceHandler<Tstate extends State>
  implements MessageHandler
{

  private ThreadLocalChannel<Tstate> reference;
  private Component comp;
  
  
  public StateReferenceHandler(Component comp,Class<Tstate> stateClass)
  { 
    this(BeanReflector.<Tstate>getInstance(stateClass));
    this.comp=comp;
   
  }

  public StateReferenceHandler(Class<Tstate> stateClass)
  { this(BeanReflector.<Tstate>getInstance(stateClass));
  }
  
  public StateReferenceHandler(Reflector<Tstate> stateReflector)
  { this.reference=new ThreadLocalChannel<Tstate>(stateReflector,true);
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    if (comp!=null)
    { reference.setDeclarationInfo(comp.getDeclarationInfo());
    }
    return focusChain;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final void handleMessage
    (Dispatcher dispatcher,Message message,MessageHandlerChain next)
  {
    reference.push((Tstate) dispatcher.getState());
    try
    { next.handleMessage(dispatcher,message);
    }
    finally
    { reference.pop();
    }
  }
  
  public Tstate get()
  { 
    try
    { return reference.get();
    }
    catch (AccessException x)
    { throw new AccessException("Component not in context "+(comp!=null?comp.getDeclarationInfo():""),x);
    }
    
  }
  
  public Reflector<Tstate> getReflector()
  { return reference.getReflector();
  }


}
