package spiralcraft.lang.parser;


public class IdentifierNode
  extends Node
{

  private final String _identifier;

  public IdentifierNode(String identifier)
  {  _identifier=identifier;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { out.append(prefix).append("Identifier:").append(_identifier);
  }

}
