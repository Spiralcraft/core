package spiralcraft.lang.parser;


public class RelationalNode
  extends Node
{

  private final boolean _greaterThan;
  private final boolean _equals;
  private final Node _op1;
  private final Node _op2;

  public RelationalNode(boolean greaterThan,boolean equals,Node op1,Node op2)
  { 
    _greaterThan=greaterThan;
    _equals=equals;
    _op1=op1;
    _op2=op2;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Relational");
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append(_greaterThan?">":"<");
    if (_equals)
    { out.append("=");
    }
    _op2.dumpTree(out,prefix);
  }

}
