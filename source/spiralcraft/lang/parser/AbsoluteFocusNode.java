package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

public class AbsoluteFocusNode
  extends FocusNode
{

  private final Node _selector;
  private final String _focusName;

  public AbsoluteFocusNode(String focusName,Node selector)
  { 
    _focusName=focusName;
    _selector=selector;
  }

  public Focus findFocus(final Focus focus)
    throws BindException
  { 
    Focus newFocus=focus.findFocus(_focusName);
    if (newFocus!=null)
    { return newFocus;
    }
    else
    { throw new BindException("Focus '"+_focusName+"' not found.");
    }
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Focus");
    prefix=prefix+"  ";
    out.append(prefix).append("name="+_focusName);
    if (_selector!=null)
    { _selector.dumpTree(out,prefix);
    }
  }
  
}
