package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

public class DefaultFocusNode
  extends FocusNode
{

  public Focus findFocus(final Focus focus)
    throws BindException
  { return focus;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { out.append(prefix).append("DefaultFocus");
  }
  
}
