package spiralcraft.lang.parser;


public class LiteralNode
  extends Node
{

  private final Object _value;
  private Class _valueClass;

  public LiteralNode(Object value,Class valueClass)
  { 
    _value=value;
    _valueClass=valueClass;
  }

  public Object getValue()
  { return _value;
  }

  public Class getValueClass(ClassLoader loader)
  {
    if (_valueClass==null)
    {
      if (_value!=null)
      { _valueClass=_value.getClass();
      }
      else
      { _valueClass=Object.class;
      }
    }
    return _valueClass;
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
}
