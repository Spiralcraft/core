package spiralcraft.xml;

/**
 * An attribute name/value pair 
 */
public class Attribute
{
  public Attribute(String name,String value)
  { 
    _name=name;
    _value=value;
  }

  public String getName()
  { return _name;
  }

  public String getValue()
  { return _value;
  }

  public String toString()
  { return "["+_name+"="+_value+"]";
  }

  private String _name;
  private String _value;
  


}
