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
package spiralcraft.text.xml;

import spiralcraft.text.ParseException;
import spiralcraft.text.LookaheadParserContext;

/**
 * Reads a literal in double quotes
 */
public class LiteralReader
{

  /**
   * Read a literal. The context should be positioned
   *   on the first quotation mark.
   */
  public void readLiteral(LookaheadParserContext context)
    throws ParseException
  { 
    _buffer=new StringBuffer();
    boolean inWhitespace=false;
    _delimiter=context.getCurrentChar();
    if (_delimiter!='"' && _delimiter!='\'')
    { 
      throw new ParseException
        ("Expected single or double quote, found '"+_delimiter+"'"
        ,context.getPosition()
        );
      
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
      { 
        throw new ParseException
          ("Unterminated delimiter ("+_delimiter+")"
          ,context.getPosition()
          );
      }
      switch (chr)
      {
      case '\n':
      case '\r':
      case '\t':
      case ' ':
        inWhitespace=true;
        break;
      default:
        if (inWhitespace)
        { 
          _buffer.append(' ');
          inWhitespace=false;
        }
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
