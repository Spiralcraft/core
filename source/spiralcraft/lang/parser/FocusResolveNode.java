package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Context;


public class FocusResolveNode
  extends Node
{

  private final FocusNode _source;
  private final IdentifierNode _identifier;

  public FocusResolveNode(FocusNode source,IdentifierNode identifier)
  { 
    _source=source;
    _identifier=identifier;
  }

  
  public Optic bind(final Focus focus)
    throws BindException
  { 
    String identifier=_identifier.getIdentifier();
    Context context;
    
    if (_source!=null)
    { context=_source.findFocus(focus).getContext();
    }
    else
    { context=focus.getContext();
    }
    if (context==null)
    { throw new BindException("Focus has no Context");
    }

    Optic ret=context.resolve(identifier);
    if (ret==null)
    { throw new BindException("Name '"+identifier+"' not found in Context.");
    }
    return ret;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("FocusResolve");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }
    _identifier.dumpTree(out,prefix);
  }
  
}
