package spiralcraft.sax;
  
/**
 * Represents an XML Attribute (a name-value pair specified within an element)
 */
public class Attribute
{
  private String _localName;
  private String _qName;
  private String _type;
  private String _uri;
  private String _value;

  /**
   * Simple constructor for no-namespace client use
   */
  public Attribute
    (String name
    ,String value
    )
  { 
    _localName=name;
    _qName=name;
    _type="";
    _uri="";
    _value=value;

  }

  public Attribute
    (String localName
    ,String qName
    ,String type
    ,String uri
    ,String value
    )
  { 
    _localName=localName;
    _qName=qName;
    _type=type;
    _uri=uri;
    _value=value;
  }

  public String getLocalName()
  { return _localName;
  }

  public String getQName()
  { return _qName;
  }

  public String getType()
  { return _type;
  }

  public String getURI()
  { return _uri;
  }

  public String getValue()
  { return _value;
  }

  public void setValue(String value)
  { _value=value;
  }

  public String toString()
  { 
    return super.toString()
      +"[uri="+_uri
      +",localName="+_localName
      +",qName="+_qName
      +",type="+_type
      +",value="+_value
      +"]"
      ;
  }

}
