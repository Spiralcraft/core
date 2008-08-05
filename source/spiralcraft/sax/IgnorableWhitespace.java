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
package spiralcraft.sax;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Represents ignorable whitespace read from an XML document
 */
public class IgnorableWhitespace
  extends Node
{
  private char[] _characters;

  public IgnorableWhitespace(char[] ch,int start,int length)
  { 
    _characters=new char[length];
    System.arraycopy(ch,start,_characters,0,length);
  }

  @Override
  public void playEvents(ContentHandler handler)
    throws SAXException
  { handler.ignorableWhitespace(_characters,0,_characters.length);
  }

}
