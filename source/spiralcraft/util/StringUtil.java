package spiralcraft.util;

import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * Useful static methods for String manipulation
 */
public class StringUtil
{

  /**
   * Tokenize a String to a String[]
   */
  public static String[] tokenize(String input,String tokens)
  {
    if (input==null)
    { return new String[0];
    }

    StringTokenizer tok=new StringTokenizer(input,tokens);
    ArrayList tokenList=new ArrayList();
    while (tok.hasMoreTokens())
    { tokenList.add(tok.nextToken());
    }
    String[] ret=new String[tokenList.size()];
    tokenList.toArray(ret);
    return ret;
  }
}
