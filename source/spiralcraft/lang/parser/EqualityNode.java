package spiralcraft.lang.parser;


public class EqualityNode
  extends Node
{

  private final boolean _negate;
  private final Node _op1;
  private final Node _op2;

  public EqualityNode(boolean negate,Node op1,Node op2)
  { 
    _negate=negate;
    _op1=op1;
    _op2=op2;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Equals");
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append(_negate?"!=":"==");
    _op2.dumpTree(out,prefix);
  }

}
