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

  public void playEvents(ContentHandler handler)
    throws SAXException
  { handler.ignorableWhitespace(_characters,0,_characters.length);
  }

}
