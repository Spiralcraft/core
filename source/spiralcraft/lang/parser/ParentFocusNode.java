package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

public class ParentFocusNode
  extends FocusNode
{

  private final FocusNode _child;

  public ParentFocusNode(FocusNode child)
  { _child=child;
  }

  public Focus findFocus(final Focus focus)
    throws BindException
  { 
    Focus childFocus;
    if (_child==null)
    { childFocus=focus;
    }
    else
    { childFocus=_child.findFocus(focus);
    }

    Focus parentFocus=childFocus.getParentFocus();
    if (parentFocus!=null)
    { return parentFocus;
    }
    else
    { throw new BindException("Focus has no parent");
    }
  }


  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("ParentFocus");
    if (_child!=null)
    {
      prefix=prefix+"  ";
      _child.dumpTree(out,prefix);
    }
  }
  
}
