package spiralcraft.sax;

import org.xml.sax.Attributes;

 
/**
 * Represents an Element in an XML document
 */
public class Element
  extends Node
{
  private String _uri;
  private String _localName;
  private String _qName;
  private Attribute[] _attributes;

  public Element
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
  { 
    _uri=uri;
    _localName=localName;
    _qName=qName;
    
    int numAttributes=attributes.getLength();
    if (numAttributes>0)
    { 
      _attributes=new Attribute[numAttributes];
      for (int i=0;i<numAttributes;i++)
      { 
        _attributes[i]=
          new Attribute
            (attributes.getLocalName(i)
            ,attributes.getQName(i)
            ,attributes.getType(i)
            ,attributes.getURI(i)
            ,attributes.getValue(i)
            )
            ;
        
      }
    }
  }

  public Attribute[] getAttributes()
  { return _attributes;
  }

  public String getURI()
  { return _uri;
  }

  public String getLocalName()
  { return _localName;
  }

  public String toString()
  { 
    return super.toString()
      +"[uri="+_uri
      +",localName="+_localName
      +",qName="+_qName
      +",attributes="+_attributes
      +"]"
      ;
  }

}
