package spiralcraft.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import spiralcraft.util.ArrayUtil;

import java.util.List;

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
  
  /**
   * Constructor for no-namespace client use
   */
  public Element
    (String name
    ,Attribute[] attributes
    )
  { 
    _uri="";
    _localName=name;
    _qName=name;
    _attributes=attributes;
  }

  /**
   * Constructor for SAX use
   */
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

  /**
   * A concatenation of the character data in this element
   */
  public String getCharacters()
  { 
    List<Characters> children
      = getChildren(Characters.class);

    StringBuilder buf=new StringBuilder();
    for (Characters c: children)
    { buf.append(c.getCharacters());
    }
    return buf.toString();
  }
  
  public void removeAttribute(Attribute attribute)
  { _attributes=(Attribute[]) ArrayUtil.remove(_attributes,attribute);
  }

  public void addAttribute(Attribute attribute)
  { _attributes=(Attribute[]) ArrayUtil.append(_attributes,attribute);
  }

  public Attribute[] getAttributes()
  { return _attributes;
  }

  public String getURI()
  { return _uri;
  }

  public String getQName()
  { return _qName;
  }

  public String getLocalName()
  { return _localName;
  }

  public void playEvents(ContentHandler handler)
    throws SAXException
  { 
    handler.startElement
      (_uri
      ,_localName
      ,_qName
      ,new ArrayAttributes(_attributes)
      );
    playChildEvents(handler);
    handler.endElement
      (_uri
      ,_localName
      ,_qName
      );
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
