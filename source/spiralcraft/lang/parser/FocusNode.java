package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

public abstract class FocusNode
  extends Node
{

  /**
   * Return another Focus related to the specified Focus
   */
  public abstract Focus findFocus(final Focus focus)
    throws BindException;
  
  public Optic bind(final Focus focus)
    throws BindException
  { return findFocus(focus).getSubject();
  }

}
