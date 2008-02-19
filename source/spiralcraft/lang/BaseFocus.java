package spiralcraft.lang;

import java.util.HashMap;

public abstract class BaseFocus<T>
  implements Focus<T>
{
  

  protected Channel<T> subject;
  protected Focus<?> parent;
  protected NamespaceResolver namespaceResolver;
  private HashMap<Expression<?>,Channel<?>> channels; 


  public Focus<?> getParentFocus()
  { return parent;
  }
    
  public synchronized void setSubject(Channel<T> val)
  { 
    subject=val;
    channels=null;
  }
  
  
  public void setParentFocus(Focus<?> parent)
  { this.parent=parent;
  }
  
  /**
   * Return the Context for this Focus, or if there is none associated,
   *   return the Context for the parent Focus.
   */
  public Channel<?> getContext()
  { return subject;
  }
  
  
  /**
   * Return the subject of expression evaluation
   */
  public Channel<T> getSubject()
  { return subject;
  }
  



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

  public NamespaceResolver getNamespaceResolver()
  { 
    if (namespaceResolver!=null)
    { return namespaceResolver;
    }
    else if (parent!=null)
    { return parent.getNamespaceResolver();
    }
    else
    { return null;
    }
  }
  
  public void setNamespaceResolver(NamespaceResolver resolver)
  { this.namespaceResolver=resolver;
  }
  
  


}
