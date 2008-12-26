//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.text;

import java.io.IOException;
import java.io.PushbackReader;

import spiralcraft.text.io.CharSequenceReader;

/**
 * <p>Tracks the stream position of a parse operation while exposing data
 *   via a pushback interface.
 * </p>
 * 
 * @author mike
 *
 */
public class PushbackParserContext
{
  private boolean eof;
  private PushbackReader _reader;
  private ParsePosition position=new ParsePosition();

  public PushbackParserContext(PushbackReader reader)
  { this._reader=reader;
  }

  public PushbackParserContext(CharSequence text)
  { this._reader=new PushbackReader(new CharSequenceReader(text));
  }

  public ParsePosition getPosition()
  { return position;
  }
  
  public void throwParseException(String message)
    throws ParseException
  { throw new ParseException(message,position);
  }
  
  public void expect(char chr)
    throws ParseException
  { 
    if (read()!=chr)
    { throwParseException("Expected '"+chr+"'");
    }
  }
  
  public boolean isEOF()
  { return eof;
  }
  
  public final int read()
    throws ParseException
  { 
    if (eof)
    { throw new ParseException("Unexpected end of input",position);
    }
    try
    {
      position.incIndex(1);
      int ret=_reader.read();
      if (ret==-1)
      { eof=true;
      }
      return ret;
    }
    catch (IOException x)
    { throw new ParseException("Caught IOException while parsing",position,x);
    }    
  }

  public final void unread(char chr)
    throws ParseException
  { 
    position.incIndex(-1);
    try
    { _reader.unread(chr);
    }
    catch (IOException x)
    { throw new ParseException("Caught IOException while parsing",position,x);
    }    
  }

}
