package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;

import spiralcraft.lang.optics.Lense;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.LenseBinding;

public class LogicalAndNode
  extends LogicalNode
{

  public LogicalAndNode(Node op1,Node op2)
  { super(op1,op2);
  }

  public Object translateForGet(Object val,Optic[] mods)
  { 
    boolean val1=val!=null?((Boolean) val).booleanValue():false;
    if (!val1)
    { return Boolean.FALSE;
    }
    Object mod=mods[0].get();
    boolean val2=mod!=null?((Boolean) mod).booleanValue():false;
    return val2?Boolean.TRUE:Boolean.FALSE;
  }
  
  public Object translateForSet(Object val,Optic[] mods)
  { 
    // Not reversible
    throw new UnsupportedOperationException();
  }
  
  public String getSymbol()
  { return "&&";
  }

}
