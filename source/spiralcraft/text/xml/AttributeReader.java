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
import spiralcraft.util.string.StringPool;

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
  public void readAttribute(LookaheadParserContext context)
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
    { throw new ParseException("Expected '='",context.getPosition());
    }
    context.advance();
    _whitespaceReader.readWhitespace(context);
    _literalReader.readLiteral(context);
    String value=_literalReader.getBuffer();
    
    _attribute
      =new Attribute
        (StringPool.INSTANCE.get(name)
        ,StringPool.INSTANCE.get(value)
        );
  }

  public Attribute getAttribute()
  { return _attribute;
  }

  
  
}
