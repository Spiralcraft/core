package spiralcraft.lang.parser;


public class MultiplyNode
  extends Node
{

  private final Node _op1;
  private final Node _op2;

  public MultiplyNode(Node op1,Node op2)
  { 
    _op1=op1;
    _op2=op2;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Multiply");
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append("*");
    _op2.dumpTree(out,prefix);
  }

}
