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

  public Characters(char[] ch,int start,int length)
  { 
    _characters=new char[length];
    System.arraycopy(ch,start,_characters,0,length);
  }

  public char[] getCharacters()
  { return _characters;
  }

  public void playEvents(ContentHandler handler)
    throws SAXException
  { handler.characters(_characters,0,_characters.length);
  }

  public String toString()
  { return super.toString()+"["+new String(_characters)+"]";
  }
}
