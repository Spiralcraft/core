package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;

public class OpNode
  extends Node
{

  private final Node _op1;
  private final Node _op2;
  private final String _op;

  public OpNode(Node op1,Node op2,char op)
  { 
    _op1=op1;
    _op2=op2;
    _op=new String(new char[]{op}).intern();
  }

  public Optic bind(final Focus focus)
    throws BindException
  { 
    Optic op1=_op1.bind(focus);

    Optic ret=op1
      .resolve(focus
              ,_op
              ,new Expression[] {new Expression(_op2,null)}
              );
    if (ret==null)
    { throw new BindException("Could not bind '"+_op+"' operator in "+op1.toString());
    }
    return ret;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Op "+_op);
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append(_op);
    _op2.dumpTree(out,prefix);
  }

}
