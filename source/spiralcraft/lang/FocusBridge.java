package spiralcraft.lang;

import java.util.HashMap;

/**
 * Provides a means for a 'client' component system (normally some sort of UI 
 *   or API client) to access a Focus exposed by another component system.
 *
 * Allows additional attributes to be associated with the Focus that are
 *   specific to the client component system.
 *
 * This allows the concrete implementation of this class to serve as
 *   a language binding 'bridge' between a user interface and the objects
 *   being manipulated by that interface.
 */
public abstract class FocusBridge
  extends ProxyFocus
{

  private DefaultContext _context;
  private Attribute[] _attributes;
  private HashMap _channels;

  protected void decorate()
  {
    if (_context==null)
    { _context=new DefaultContext(super.getContext());
    }
    _context.setAttributes(_attributes);
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

  public Context getContext()
  { return _context;
  }

  public void setAttributes(Attribute[] attribs)
  { _attributes=attribs;
  }

}
