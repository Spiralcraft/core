package spiralcraft.lang.parser;


public class LogicalNegateNode
  extends Node
{

  private final Node _node;

  public LogicalNegateNode(Node node)
  {  _node=node;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Not");
    prefix=prefix+"  ";
    _node.dumpTree(out,prefix);
  }

}
