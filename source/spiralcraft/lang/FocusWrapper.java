package spiralcraft.lang;

import java.net.URI;
import java.util.HashMap;


public class FocusWrapper<tFocus>
  implements Focus<tFocus>
{
  protected final Focus<tFocus> focus;
  private HashMap<Expression<?>,Channel<?>> channels;
  
  public FocusWrapper(Focus<tFocus> delegate)
  { this.focus=delegate;
  }
  
  
  /**
   * Bind is overridden to maintain wrapper
   */
  @Override
  @SuppressWarnings("unchecked") // Heterogeneous hash map
  public synchronized <X> Channel<X> bind(Expression<X> expression)
    throws BindException
  { 
    Channel<X> channel=null;
    if (channels==null)
    { channels=new HashMap<Expression<?>,Channel<?>>();
    }
    else
    { channel=(Channel<X>) channels.get(expression);
    }
    if (channel==null)
    { 
      channel=expression.bind(this);
      channels.put(expression,channel);
    }
    return channel;
  }


//  @Override
//  public <X> Channel<X> bind(
//    Expression<X> expression)
//    throws BindException
//  {  return focus.bind(expression);
//  }

  @Override
  public Focus<?> findFocus(
    URI specifier)
  { return focus.findFocus(specifier);
  }

  @Override
  public Channel<?> getContext()
  { return focus.getContext();
  }

  @Override
  public NamespaceResolver getNamespaceResolver()
  { return focus.getNamespaceResolver();
  }

  @Override
  public Focus<?> getParentFocus()
  { return focus.getParentFocus();
  }

  @Override
  public Channel<tFocus> getSubject()
  { return focus.getSubject();
  }

  @Override
  public boolean isFocus(
    URI specifier)
  { return focus.isFocus(specifier);
  }

}
