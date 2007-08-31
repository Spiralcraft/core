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
