package spiralcraft.lang.parser;

import spiralcraft.lang.optics.StringOptic;
import spiralcraft.lang.optics.NumberOptic;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;

import java.math.BigDecimal;

public class AddNode
  extends Node
{

  private final Node _op1;
  private final Node _op2;

  public AddNode(Node op1,Node op2)
  { 
    _op1=op1;
    _op2=op2;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Add");
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append("+");
    _op2.dumpTree(out,prefix);
  }

  public Optic bind(final Focus focus)
    throws BindException
  { 
    final Optic op1=_op1.bind(focus);
    final Optic op2=_op2.bind(focus);

    if (op1 instanceof NumberOptic)
    { 
      return new NumberOptic()
      {
        public Object get()
        { 
          Number val1=(Number) op1.get();
          Number val2=(Number) op2.get();
          if (val1==null || val2==null)
          { return null;
          }
          return new BigDecimal(val1.toString()).add(new BigDecimal(val2.toString()));
        }
      };
    }
    else if (op1 instanceof StringOptic)
    { 
      return new StringOptic()
      {
        public Object get()
        { 
          final String val1=(String) op1.get();
          final String val2=(String) op2.get();
          if (val2==null)
          { return val1;
          }
          if (val1==null)
          { return val2;
          }
          return val1+val2;
        }
      };
    }
    else
    { throw new BindException("Can't apply operator '+'");
    }

  }
}
