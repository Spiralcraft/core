package spiralcraft.lang.parser;


public class NumericNegateNode
  extends Node
{

  private final Node _node;

  public NumericNegateNode(Node node)
  {  _node=node;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Negative");
    prefix=prefix+"  ";
    _node.dumpTree(out,prefix);
  }

}
