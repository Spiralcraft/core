package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;


public class ResolveNode
  extends Node
{

  private final Node _source;
  private final IdentifierNode _identifier;

  public ResolveNode(Node source,IdentifierNode identifier)
  { 
    _source=source;
    _identifier=identifier;
  }

  public String getIdentifierName()
  { return _identifier.getIdentifier();
  }

  public Node getSource()
  { return _source;
  }

  public Optic bind(final Focus focus)
    throws BindException
  { 
    Optic sourceOptic;
    if (_source!=null)
    { sourceOptic=_source.bind(focus);
    }
    else
    { sourceOptic=focus.getSubject();
    }
    String identifier=_identifier.getIdentifier();

    Optic ret=sourceOptic.resolve(focus,identifier,null);
    if (ret==null)
    { throw new BindException("Name '"+identifier+"' not found.");
    }
    return ret;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Resolve");
    prefix=prefix+"  ";
    if (_source!=null)
    { _source.dumpTree(out,prefix);
    }
    out.append(prefix).append(".");
    _identifier.dumpTree(out,prefix);
  }
  
}
