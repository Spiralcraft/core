package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;

import spiralcraft.lang.optics.Lense;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.LenseBinding;

public class LogicalOrNode
  extends LogicalNode
{

  public LogicalOrNode(Node op1,Node op2)
  { super(op1,op2);
  }

  public Object translateForGet(Object val,Object[] mods)
  { 
    boolean val1=val!=null?((Boolean) val).booleanValue():false;
    boolean val2=mods[0]!=null?((Boolean) mods[0]).booleanValue():false;
    return (val1 || val2)?Boolean.TRUE:Boolean.FALSE;
  }
  
  public Object translateForSet(Object val,Object[] mods)
  { 
    // Not reversible
    throw new UnsupportedOperationException();
  }
  
  public String getSymbol()
  { return "||";
  }

}
