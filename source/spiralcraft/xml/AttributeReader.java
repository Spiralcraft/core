package spiralcraft.xml;

/**
 * Reads and XML attribute name/value pair
 */
public class AttributeReader
{
  private NameTokenReader _nameTokenReader
    =new NameTokenReader();
  
  private WhitespaceReader _whitespaceReader
    =new WhitespaceReader();
 
  private LiteralReader _literalReader
    =new LiteralReader();

  private Attribute _attribute;

  /**
   * Read an attribute/value. The context should be
   *   positioned on the first character of the
   *   attribute name token.
   */
  public void readAttribute(ParserContext context)
    throws ParseException
  {
    _attribute=null;
    _nameTokenReader.readNameToken(context);
    String name=_nameTokenReader.getBuffer();
    if (name.length()==0)
    { return;
    }
    _whitespaceReader.readWhitespace(context);
    if (context.getCurrentChar()!='=')
    { throw new ParseException("Expected '='");
    }
    context.advance();
    _whitespaceReader.readWhitespace(context);
    _literalReader.readLiteral(context);
    String value=_literalReader.getBuffer();
    _attribute=new Attribute(name.intern(),value.intern());
  }

  public Attribute getAttribute()
  { return _attribute;
  }

  
  
}
