package spiralcraft.lang.parser;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.SimpleOptic;

import spiralcraft.lang.OpticAdapter;

import java.math.BigDecimal;

public class LiteralNode
  extends Node
{

  private final SimpleOptic _optic;

  public LiteralNode(Object value,Class valueClass)
  { _optic=new SimpleOptic(valueClass,value);
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix)
      .append("Literal: ")
      .append(_optic.toString())
      ;
  }

  public Optic bind(final Focus focus)
    throws BindException
  { return _optic;
  }
}
