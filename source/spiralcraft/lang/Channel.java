package spiralcraft.lang;

import spiralcraft.lang.optics.ProxyOptic;

public class Channel
  extends ProxyOptic
{
  private Expression _expression;

  public Channel(Optic optic,Expression expression)
  { 
    super(optic);
    _expression=expression;
  }

  public Expression getExpression()
  { return _expression;
  }
}
