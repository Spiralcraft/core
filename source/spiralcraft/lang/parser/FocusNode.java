package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

public class FocusNode
  extends Node
{

  private final Node _selector;
  private final String _focusName;

  public FocusNode(String focusName,Node selector)
  { 
    _focusName=focusName;
    _selector=selector;
  }

  public Focus findFocus(final Focus focus)
    throws BindException
  { return focus.findFocus(_focusName);
  }

  public Optic bind(final Focus focus)
    throws BindException
  { 
    Focus newFocus=findFocus(focus);
    if (newFocus!=null)
    { return newFocus.getSubject();
    }
    else
    { throw new BindException("Singleton '"+_focusName+"' not found.");
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
