package spiralcraft.lang.parser;


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

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Resolve");
    prefix=prefix+"  ";
    _source.dumpTree(out,prefix);
    out.append(prefix).append(".");
    _identifier.dumpTree(out,prefix);
  }
  
}
