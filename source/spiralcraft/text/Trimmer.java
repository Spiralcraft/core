package spiralcraft.text;

import java.util.HashMap;

/**
 * Trims text from CharSequences 
 */
public class Trimmer
{
  private final String _chars;
  
  /**
   * Construct a trimmer which trims the specified chars
   */
  public Trimmer(String chars)
  { _chars=chars;
  }

  /**
   * Trim text from both ends of the given CharSequence
   */
  public CharSequence trim(CharSequence original)
  {
    final int length=original.length();
    int start=0;
    int end=length;

    while (start<length
          && _chars.indexOf(original.charAt(start))>-1
          )
    { start++;
    }

    while (end>0
          && _chars.indexOf(original.charAt(end-1))>-1
          )
    { end--;
    }
    
    return original.subSequence(start,end);
  }

}
