package spiralcraft.xml;

/**
 * XML whitespace
 */
public class WhitespaceReader
{

  private StringBuffer _buffer;

  public void readWhitespace(ParserContext context)
    throws ParseException
  {
    _buffer=new StringBuffer();
    while (!context.isEof())
    {
      char chr=context.getCurrentChar();
      switch (chr)
      {
      case ' ':
      case '\t':
      case '\r':
      case '\n':
        _buffer.append(chr);
        context.advance();
        continue;
      }
      return;
    }
  }

  public String getBuffer()
  { return _buffer.toString();
  }

}
