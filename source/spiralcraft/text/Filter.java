package spiralcraft.text;

import java.util.HashMap;

/**
 * Filters text from CharSequences 
 */
public class Filter
{
  private final String _chars;
  
  /**
   * Construct a Filter which filters the specified chars
   */
  public Filter(String chars)
  { _chars=chars;
  }

  /**
   * Filter characters from the given CharSequence
   */
  public String filter(CharSequence original)
  {
    char[] out=new char[original.length()];
    int pos=0;
    for (int i=0;i<original.length();i++)
    { 
      char ch=original.charAt(i);
      if (_chars.indexOf(ch)==-1)
      { out[pos++]=ch;
      }
    }
    return new String(out,0,pos);
  }

}
