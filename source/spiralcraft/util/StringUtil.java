package spiralcraft.util;

import spiralcraft.text.Filter;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;

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

  /**
   * Efficiently turn a string into an ascii byte array
   */
  public static byte[] asciiBytes(String string)
  {
    byte[] bytes=new byte[string.length()];
    for (int i=0;i<bytes.length;i++)
    { bytes[i]=(byte) string.charAt(i);
    }
    return bytes;
  }
  
  /**
   * Remove the specified chars from the source string
   */
  public static String removeChars(CharSequence source,String chars)
  {
    Filter filter=new Filter(chars);
    return filter.filter(source);
  }

  /**
   * Find an instance of any of the specified chars
   */
  public static int findAny(CharSequence source,int start,String chars)
  {
    for (int i=start;i<source.length();i++)
    { 
      if (chars.indexOf(source.charAt(i))>-1)
      { return i;
      }
    }
    return -1;
    
  }
}
