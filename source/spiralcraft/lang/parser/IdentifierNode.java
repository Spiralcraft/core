package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Context;
import spiralcraft.lang.BindException;

public class IdentifierNode
  extends Node
{

  private final String _identifier;

  public IdentifierNode(String identifier)
  {  _identifier=identifier.intern();
  }

  public String getIdentifier()
  { return _identifier;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { out.append(prefix).append("Identifier:").append(_identifier);
  }

  public Optic bind(Focus focus)
    throws BindException
  { 
    Context context=focus.getContext();
    if (context==null)
    { throw new BindException("No Context to resolve '"+_identifier+"'");
    }

    Optic ret=context.resolve(_identifier);
    if (ret==null)
    { throw new BindException("Name '"+_identifier+"' not found in Context");
    }
    return ret;
  }
}
