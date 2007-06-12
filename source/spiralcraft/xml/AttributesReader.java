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

import java.util.LinkedList;

import spiralcraft.text.ParseException;

/**
 * A set of attributes in an XML tag
 */
public class AttributesReader
{

  private Attribute[] _attributes;
  
  private WhitespaceReader _whitespaceReader
    =new WhitespaceReader();

  private AttributeReader _attributeReader
    =new AttributeReader();

  /**
   * Read a list of attributes. The context should be positioned
   *   on the first attribute name token.
   */
  public void read(ParserContext context)
    throws ParseException
  { 
    LinkedList<Attribute> attributes=new LinkedList<Attribute>();
    while (!context.isEof())
    {
      _attributeReader.readAttribute(context);
      if (_attributeReader.getAttribute()!=null)
      { attributes.add(_attributeReader.getAttribute());
      }
      else
      { return;
      }
      _whitespaceReader.readWhitespace(context);
    }
    _attributes=new Attribute[attributes.size()];
    attributes.toArray(_attributes);
  }
    
  public Attribute[] getAttributes()
  { return _attributes;
  }
  
}
