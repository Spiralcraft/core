package spiralcraft.lang;

import java.util.HashMap;

/**
 * Decorates the Environment of another focus
 */
public abstract class FocusDecorator
  extends ProxyFocus
{

  private DefaultEnvironment _environment;
  private Attribute[] _attributes;
  private HashMap _channels;

  protected void decorate()
  {
    if (_environment==null)
    { _environment=new DefaultEnvironment(super.getEnvironment());
    }
    _environment.setAttributes(_attributes);
  }

  public synchronized Channel bind(Expression expression)
    throws BindException
  { 
    Channel channel=null;
    if (_channels==null)
    { _channels=new HashMap();
    }
    else
    { channel=(Channel) _channels.get(expression);
    }
    if (channel==null)
    { 
      channel=expression.bind(this);
      _channels.put(expression,channel);
    }
    return channel;
  }

  public Environment getEnvironment()
  { return _environment;
  }

  public void setAttributes(Attribute[] attribs)
  { _attributes=attribs;
  }

}
