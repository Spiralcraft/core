package spiralcraft.xml;

/**
 * Reads a literal in double quotes
 */
public class LiteralReader
{

  /**
   * Read a literal. The context should be positioned
   *   on the first quotation mark.
   */
  public void readLiteral(ParserContext context)
    throws ParseException
  { 
    _buffer=new StringBuffer();
    _delimiter=context.getCurrentChar();
    if (_delimiter!='"' && _delimiter!='\'')
    { 
      throw new ParseException
        ("Expected single or double quote, found '"+_delimiter+"'");
    }
    context.advance();
    
    while (true)
    {
      char chr=context.getCurrentChar();
      if (chr==_delimiter)
      {
        context.advance();
        return;
      }
      if (context.isEof())
      { throw new ParseException("Unterminated delimiter ("+_delimiter+")");
      }
      switch (chr)
      {
      case '\n':
      case '\r':
        throw new ParseException("Found line break in literal");
      default:
        _buffer.append(chr);
      }
      context.advance();
    }    
  }

  public String getBuffer()
  { return _buffer.toString();
  }

  private char _delimiter;
  private StringBuffer _buffer;
}
