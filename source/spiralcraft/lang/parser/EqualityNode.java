package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;

import spiralcraft.lang.optics.ProxyOptic;

public class EqualityNode
  extends LogicalNode
{

  private final boolean _negate;

  public EqualityNode(boolean negate,Node op1,Node op2)
  { 
    super(op1,op2);
    _negate=negate;
  }

  public Object translateForGet(Object val,Optic[] mods)
  { 
    Object mod=mods[0].get();
    if (val==mod)
    { return _negate?Boolean.FALSE:Boolean.TRUE;
    }
    else if (val!=null && val.equals(mod))
    { return _negate?Boolean.FALSE:Boolean.TRUE;
    }
    return _negate?Boolean.TRUE:Boolean.FALSE;
  }
  
  public Object translateForSet(Object val,Optic[] mods)
  { 
    // Not reversible
    throw new UnsupportedOperationException();
  }
  
  public String getSymbol()
  { return _negate?"!=":"==";
  }

}
