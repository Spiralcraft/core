package spiralcraft.lang;

import java.util.HashMap;

/**
 * Simple implementation of Focus
 */
public class DefaultFocus
  implements Focus,Environment
{

  private Environment _environment;
  private Optic _subject;
  private Focus _parent;
  private HashMap _channels;

  public DefaultFocus()
  {
  }

  public DefaultFocus(Optic subject)
  { _subject=subject;
  }

  public Optic resolve(String name)
  { 
    try
    { return _subject.resolve(this,name,null);
    }
    catch (BindException x)
    { return null;
    }
  }

  public String[] getNames()
  { 
    // XXX Get names from the subject
    return null;
  }

  public void setParentFocus(Focus parent)
  { _parent=parent;
  }

  public Focus getParentFocus()
  { return _parent;
  }

  public void setEnvironment(Environment val)
  { _environment=val;
  }
    
  public synchronized void setSubject(Optic val)
  { 
    _subject=val;
    _channels=null;
  }

  /**
   * Return the Environment which resolves
   *   names for this Focus. If no environment
   *   was configured, Environment names will be resolved
   *   against the subject.
   */
  public Environment getEnvironment()
  { 
    if (_environment==null)
    { _environment=this;
    }
    return _environment;
  }

  /**
   * Return the subject of expression evaluation
   */
  public Optic getSubject()
  { return _subject;
  }

  public Focus findFocus(String name)
  { return _parent.findFocus(name);
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
