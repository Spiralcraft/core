package spiralcraft.sax;
  

public class Attribute
{
  private String _localName;
  private String _qName;
  private String _type;
  private String _uri;
  private String _value;

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
