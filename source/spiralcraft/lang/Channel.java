package spiralcraft.lang;

import spiralcraft.lang.optics.ProxyOptic;

import java.util.HashMap;

/**
 * A new Focus created as a result of binding an Expression to a source Focus.
 *
 * This Focus inherits the Context of the source Focus and has a subject
 *   that corresponds to the data pathway specifed by the expression.
 */
public class Channel
  extends ProxyOptic
  implements Focus
{
  private final Expression _expression;
  private final Focus _source;
  private HashMap _channels;

  public Channel(Focus source,Optic optic,Expression expression)
  { 
    super(optic);
    _expression=expression;
    _source=source;
  }

  public Expression getExpression()
  { return _expression;
  }
  
  public Optic getSubject()
  { return this;
  }
  
  public Focus getParentFocus()
  { return _source;
  }
  
  public Focus findFocus(String name)
  { return _source.findFocus(name);
  }
  
  public Context getContext()
  { return _source.getContext();
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
