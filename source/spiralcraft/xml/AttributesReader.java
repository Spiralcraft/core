package spiralcraft.xml;

import java.util.LinkedList;

/**
 * A set of attributes in an XML tag
 */
public class AttributesReader
{

  private Attribute[] _attributes;
  
  private WhitespaceReader _whitespaceReader
    =new WhitespaceReader();

  private AttributeReader _attributeReader
    =new AttributeReader();

  /**
   * Read a list of attributes. The context should be positioned
   *   on the first attribute name token.
   */
  public void read(ParserContext context)
    throws ParseException
  { 
    LinkedList attributes=new LinkedList();
    while (!context.isEof())
    {
      _attributeReader.readAttribute(context);
      if (_attributeReader.getAttribute()!=null)
      { attributes.add(_attributeReader.getAttribute());
      }
      else
      { return;
      }
      _whitespaceReader.readWhitespace(context);
    }
    _attributes=new Attribute[attributes.size()];
    attributes.toArray(_attributes);
  }
    
  public Attribute[] getAttributes()
  { return _attributes;
  }
  
}
