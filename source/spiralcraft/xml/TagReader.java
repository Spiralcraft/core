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

import spiralcraft.util.ArrayUtil;

/**
 * Reads an XML tag (name and attributes)
 */
public class TagReader
{
  private NameTokenReader _nameTokenReader
    =new NameTokenReader();
  
  private WhitespaceReader _whitespaceReader
    =new WhitespaceReader();
 
  private AttributesReader _attributesReader
    =new AttributesReader();

  // private Attribute[] _attributes;
  private String _tagName;

  
  public String getTagName()
  { return _tagName;
  }

  public Attribute[] getAttributes()
  { return _attributesReader.getAttributes();
  }

  /**
   * Read a Tag from the markup sequence provided. The provided
   *   markup sequence must not contain the tag delimiters.
   */
  public void readTag(CharSequence markup)
    throws ParseException
  { readTag(new ParserContext(markup));
  }
  
  /**
   * Read a tag. The context should be
   *   positioned on the first character of the
   *   tag name.
   */
  public void readTag(ParserContext context)
    throws ParseException
  {
    _nameTokenReader.readNameToken(context);
    _tagName=_nameTokenReader.getBuffer();
    _whitespaceReader.readWhitespace(context);
    _attributesReader.read(context);
    // _attributes=_attributesReader.getAttributes();
  }

  public String toString()
  { 
    return 
      super.toString()
        +"[name='"+_tagName
        +"',attributes="+ArrayUtil.format(getAttributes(),",","")+"]"
        ;
  }
}
