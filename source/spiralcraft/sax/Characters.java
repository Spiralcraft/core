package spiralcraft.sax;

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

  public String toString()
  { return super.toString()+"["+new String(_characters)+"]";
  }
}
