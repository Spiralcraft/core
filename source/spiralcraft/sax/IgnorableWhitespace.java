package spiralcraft.sax;

public class IgnorableWhitespace
  extends Node
{
  private char[] _characters;

  public IgnorableWhitespace(char[] ch,int start,int length)
  { 
    _characters=new char[length];
    System.arraycopy(ch,start,_characters,0,length);
  }

}
