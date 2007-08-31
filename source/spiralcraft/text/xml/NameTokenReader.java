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
