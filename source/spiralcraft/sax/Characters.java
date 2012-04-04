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

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

/**
 * Represents a sequence of characters in an XML document
 */
public class Characters
  extends Node
{
  private char[] _characters;

  public Characters(String characters)
  { _characters=characters.toCharArray();
  }
  
  public Characters(char[] ch,int start,int length)
  { 
    _characters=new char[length];
    System.arraycopy(ch,start,_characters,0,length);
  }

  public char[] getCharacters()
  { return _characters;
  }

  public void setCharacters(char[] characters)
  { _characters=characters;
  }
  
  @Override
  public void playEvents(ContentHandler handler)
    throws SAXException
  { handler.characters(_characters,0,_characters.length);
  }

  @Override
  public String toString()
  { return super.toString()+"["+new String(_characters)+"]";
  }
}
