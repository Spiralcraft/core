package spiralcraft.util;

/**
 * Associates a String name with a value
 */
public class NamedValue
{
  private String _name;
  private Object _value;

  public NamedValue()
  {
  }

  public void setName(String name)
  { _name=name;
  }

  public void setValue(Object value)
  { _value=value;
  }

  public String getName()
  { return _name;
  }

  public Object getValue()
  { return _value;
  }
}
