package spiralcraft.lang;

import java.util.HashMap;

/**
 * Simple implementation of Focus
 */
public class DefaultFocus
  implements Focus
{

  private Context _context;
  private Optic _subject;
  private Focus _parent;
  private HashMap _channels;

  public DefaultFocus()
  {
  }

  public DefaultFocus(Optic subject)
  { _subject=subject;
  }

  public void setParentFocus(Focus parent)
  { _parent=parent;
  }

  public Focus getParentFocus()
  { return _parent;
  }

  public void setContext(Context val)
  { _context=val;
  }
    
  public synchronized void setSubject(Optic val)
  { 
    _subject=val;
    _channels=null;
  }

  /**
   * Return the Context for this Focus, or if there is none associated,
   *   return the Context for the parent Focus.
   */
  public Context getContext()
  { 
    if (_context!=null)
    { return _context;
    }
    else if (_parent!=null)
    { return _parent.getContext();
    }
    else
    { return null;
    }
    
  }

  /**
   * Return the subject of expression evaluation
   */
  public Optic getSubject()
  { return _subject;
  }

  public Focus findFocus(String name)
  { 
    if (_parent!=null)
    { return _parent.findFocus(name);
    }
    else
    { return null;
    }
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
}
