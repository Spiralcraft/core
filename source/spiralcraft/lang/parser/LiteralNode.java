package spiralcraft.lang.parser;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.SimpleOptic;
import spiralcraft.lang.optics.SimpleBinding;


import java.math.BigDecimal;

public class LiteralNode
  extends Node
{

  private final SimpleOptic _optic;

  public LiteralNode(Object value,Class valueClass)
  { 
    try
    { _optic=new SimpleOptic(new SimpleBinding(valueClass,value,true));
    }
    catch (BindException x)
    { throw new IllegalArgumentException(x.toString());
    }
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix)
      .append("Literal: ")
      .append(_optic.toString())
      ;
  }

  public synchronized Optic bind(final Focus focus)
    throws BindException
  { return _optic;
  }
}
