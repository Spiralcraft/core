package spiralcraft.lang;

import spiralcraft.lang.optics.ProxyOptic;

import java.util.HashMap;

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
  
  public Environment getEnvironment()
  { return _source.getEnvironment();
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
