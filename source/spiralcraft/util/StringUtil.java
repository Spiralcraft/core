package spiralcraft.util;

import spiralcraft.text.Filter;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Useful static methods for String manipulation
 */
public class StringUtil
{

  /**
   * Tokenize a String into a List
   */
  public static int tokenize(String input,String tokens,List output)
  {
    if (input==null)
    { return 0;
    }

    StringTokenizer tok=new StringTokenizer(input,tokens);
    int count=0;
    while (tok.hasMoreTokens())
    { 
      count++;
      output.add(tok.nextToken());
    }
    return count;
  }

  /**
   * Tokenize a String to a String[]
   */
  public static String[] tokenize(String input,String tokens)
  {
    if (input==null)
    { return new String[0];
    }

    List tokenList=new LinkedList();
    tokenize(input,tokens,tokenList);
    String[] ret=new String[tokenList.size()];
    tokenList.toArray(ret);
    return ret;
  }

  /**
   * Tokenize a String as a command line
   *
   * Each command line is composed of several tokens separated
   * by whitespace. 
   *
   * A double quoted string is a single token, whether or not it
   * contains internal whitespace.
   * 
   * A token which contains double quotes can escape the double quotes by
   *   prefixing them with a backslash ('\') character.
   *
   *XXX Make this more reliable for embedded quotes and escape chars
   *XXX We might want to dispense with the StreamTokenizer and take care
   *XXX of this simple task by hand.
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
