package spiralcraft.xml;

/**
 * Reads and XML tag (name and attributes)
 */
public class TagReader
{
  private NameTokenReader _nameTokenReader
    =new NameTokenReader();
  
  private WhitespaceReader _whitespaceReader
    =new WhitespaceReader();
 
  private AttributesReader _attributesReader
    =new AttributesReader();

  private Attribute[] _attributes;
  private String _tagName;

  public String getTagName()
  { return _tagName;
  }

  public Attribute[] getAttributes()
  { return _attributesReader.getAttributes();
  }

  /**
   * Read a tag. The context should be
   *   positioned on the first character of the
   *   tag name.
   */
  public void readTag(ParserContext context)
    throws ParseException
  {
    _nameTokenReader.readNameToken(context);
    _tagName=_nameTokenReader.getBuffer();
    _whitespaceReader.readWhitespace(context);
    _attributesReader.read(context);
    _attributes=_attributesReader.getAttributes();
  }


}
