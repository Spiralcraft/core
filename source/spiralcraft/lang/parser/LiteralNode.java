package spiralcraft.lang.parser;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.StringOptic;
import spiralcraft.lang.optics.NumberOptic;

import spiralcraft.lang.OpticAdapter;

import java.math.BigDecimal;

public class LiteralNode
  extends Node
{

  private Class _valueClass;
  private Object _value;

  public LiteralNode(Object value,Class valueClass)
  { 
    _value=value;
    _valueClass=valueClass;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix)
      .append("Literal:")
      .append(_valueClass.getName())
      .append(":")
      .append(_value)
      ;
  }

  public Optic bind(final Focus focus)
    throws BindException
  { 
    if (_valueClass==String.class)
    { 
      return new StringOptic()
      {
        public Object get()
        { return _value;
        }
      };
    }
    else if (_valueClass==BigDecimal.class)
    { 
      return new NumberOptic()
      {
        public Object get()
        { return _value;
        }
      };
    }
    else if (Number.class.isAssignableFrom(_valueClass))
    { 
      return new NumberOptic()
      {
        public Object get()
        { return _value;
        }
      };
    }
    else
    { throw new BindException("Could not create optic for "+_valueClass.getName());
    }
  }
}
