package spiralcraft.util;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;

import java.util.StringTokenizer;
import java.util.ArrayList;


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

  /**
   * Tokenize a String as a command line
   *
   * Command lines ignore whitespace, except within double quoted
   *   strings.
   */
  public static String[] tokenizeCommandLine(String input)
  {
    ArrayList result=new ArrayList(10);
    StreamTokenizer st=new StreamTokenizer(new StringReader(input));
    st.resetSyntax();
    st.eolIsSignificant(false);
    st.whitespaceChars(' ',' ');
    st.whitespaceChars((char) 0,(char) 31);
    st.wordChars((char) 33,(char) 127);
    st.quoteChar('"');
    st.wordChars('\\','\\');
    
    try
    { 
      st.nextToken();	
      while (st.ttype!=st.TT_EOF)
      {
        if (st.ttype=='"' || st.ttype==st.TT_WORD)
        {	result.add(st.sval);
        }
        st.nextToken();
      }
    }
    catch (IOException x)
    { }

    String[] ret=new String[result.size()];
    result.toArray(ret);
    return ret;
  }
}
