package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Environment;
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
    Environment environment=focus.getEnvironment();
    if (environment==null)
    { throw new BindException("No environment to resolve '"+_identifier+"'");
    }

    Optic ret=environment.resolve(_identifier);
    if (ret==null)
    { throw new BindException("Name '"+_identifier+"' not found in environment");
    }
    return ret;
  }
}
