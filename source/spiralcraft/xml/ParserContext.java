//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.xml;

import java.io.Reader;
import java.io.IOException;

import spiralcraft.text.io.CharSequenceReader;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;

/**
 * Encapsulates the state of parsing an XML document
 */
public class ParserContext
{
  private char _currentChar;
  private boolean _eof=false;
  private Reader _reader;
  private ParsePosition position=new ParsePosition();

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

  public ParsePosition getPosition()
  { return position;
  }
  
  public final char getCurrentChar()
  { return _currentChar;
  }

  public final void advance()
    throws ParseException
  { 
    if (_eof)
    { throw new ParseException("Unexpected end of input",position);
    }
    try
    {
      
      int chr=_reader.read();
      position.incIndex(1);
      if (chr==-1)
      { _eof=true;
      }
      else
      { _currentChar=(char) chr;
      }
    }
    catch (IOException x)
    { throw new ParseException("",position,x);
    }
  }

  public final boolean isEof()
  { return _eof;
  }

  
}
