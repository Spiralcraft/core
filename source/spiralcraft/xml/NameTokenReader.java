package spiralcraft.xml;

/**
 * An XML name token (tag, entity or attribute name)
 */
public class NameTokenReader
{

  /**
   * Read a name token. The context should be positioned
   *   on the first character.
   */
  public void readNameToken(ParserContext context)
    throws ParseException
  {
    _buffer=new StringBuffer();

    while (!context.isEof())
    {
      char chr=context.getCurrentChar();
      switch (chr)
      {
      case '%':
      case '<':
      case '>':
      case '&':
      case ',':
      case '|':
      case '*':
      case '+':
      case '?':
      case ')':
      case '=':
      case '\'':
      case '"':
      case '[':
      case ' ':
      case '\t':
      case '\n':
      case '\r':
      case ';':
      case '/':
        return;
      default:
        _buffer.append(chr);
        context.advance();
        continue;
      }
    }
  }

  public String getBuffer()
  { return _buffer.toString();
  }

  private StringBuffer _buffer;
}
