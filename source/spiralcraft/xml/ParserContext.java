package spiralcraft.xml;

import java.io.Reader;
import java.io.IOException;

import spiralcraft.text.io.CharSequenceReader;

/**
 * Encapsulates the state of parsing an XML document
 */
public class ParserContext
{
  private char _currentChar;
  private boolean _eof=false;
  private Reader _reader;

  public ParserContext(Reader reader)
    throws ParseException
  { 
    _reader=reader;
    advance();
  }

  public ParserContext(CharSequence text)
    throws ParseException
  { 
    _reader=new CharSequenceReader(text);
    advance();
  }

  public final char getCurrentChar()
  { return _currentChar;
  }

  public final void advance()
    throws ParseException
  { 
    if (_eof)
    { throw new ParseException("Unexpected end of input");
    }
    try
    {
      int chr=_reader.read();
      if (chr==-1)
      { _eof=true;
      }
      else
      { _currentChar=(char) chr;
      }
    }
    catch (IOException x)
    { throw new ParseException("",x);
    }
  }

  public final boolean isEof()
  { return _eof;
  }

  
}
