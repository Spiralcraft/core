//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.util.string;

import spiralcraft.text.Filter;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
   * <p>Tokenize a String into a List</p>
   * 
   */
  public static int tokenize
    (String input,String tokens,List<String> output,boolean includeTokens)
  {
    if (input==null)
    { return 0;
    }

    StringTokenizer tok=new StringTokenizer(input,tokens,includeTokens);
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

    List<String> tokenList=new LinkedList<String>();
    tokenize(input,tokens,tokenList,false);
    String[] ret=new String[tokenList.size()];
    tokenList.toArray(ret);
    return ret;
  }

  /**
   * Tokenize a String to a String[]
   */
  public static String[] tokenize
    (String input
    ,String tokens
    ,boolean includeTokens
    )
  {
    if (input==null)
    { return new String[0];
    }

    List<String> tokenList=new LinkedList<String>();
    tokenize(input,tokens,tokenList,includeTokens);
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
    ArrayList<String> result=new ArrayList<String>(10);
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
      while (st.ttype!=StreamTokenizer.TT_EOF)
      {
        if (st.ttype=='"' || st.ttype==StreamTokenizer.TT_WORD)
        {	result.add(st.sval);
        }
        
        // System.err.println("StringUtil.tokenizeCommandLine: "+st.sval);
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
   * Remove the part of the String including and after the specified
   *   character, if it exists.
   */
  public static String discardAfter(String string,char chr)
  {
    if (string==null)
    { return null;
    }
    
    int chrPos=string.indexOf(chr);
    if (chrPos>=0)
    { return string.substring(0,chrPos);
    }
    else
    { return string;
    }
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
  
  /**
   * Repeat the specified string the specified number of occurrences.
   * 
   * @param string
   * @param occurrences
   * @return A StringBuilder containing the repeated string
   */
  public static StringBuilder repeat(String string,int occurrences)
  {
    StringBuilder ret=new StringBuilder();
    if (string==null)
    { return ret;
    }
    for (int i=0;i<occurrences;i++)
    { ret.append(string);
    }
    return ret;
  }
  
  public static String escapeISOControl(String string)
  {
    StringBuilder ret=new StringBuilder();
    for (char c:string.toCharArray())
    {
      if (Character.isISOControl(c))
      { ret.append("\\u"+Integer.toString(c,16));
      }
      else
      { ret.append(c);
      }
    }
    return ret.toString();
  }
  
  public static String escapeToASCII(String string)
  { 
    try
    { return new String(string.getBytes("ASCII"));
    }
    catch (UnsupportedEncodingException x)
    { throw new IllegalArgumentException(x);
    }

  }
  
  /**
   * Returns a char[] initialized to the specified character
   * 
   * @param c
   * @param count
   * @return
   */
  public static char[] repeat(char c,int count)
  {
    char[] ret=new char[count];
    for (int i=0;i<ret.length;i++)
    { ret[i]=c;
    }
    return ret;
  }

  /**
   * Pads input by inserting c at the beginning to ensure minimum length.
   * 
   * @param input
   * @param c
   * @param length
   * @return
   */
  public static String prepad(String input,char c,int length)
  {
    int diff=length-input.length();
    switch (diff)
    {
      case 0:
        return input;
      case 1:
        return c+input;
      case 2:
        return c+c+input;
      default:
        StringBuilder b=new StringBuilder();
        b.append(repeat(c,diff));
        b.append(input);
        return b.toString();
    }
  }
  
  /**
   * Escapes charsToEscape by preceeding them with the escape char. The
   *   escapeChar is automatically escaped.
   * 
   * @param input
   * @param escapeChar
   * @param charsToEscape
   * @return
   */
  public static String escape(String input,char escapeChar,String charsToEscape)
  {
    final StringBuilder out=new StringBuilder();
    for (char c:input.toCharArray())
    {
      if (charsToEscape.indexOf(c)>-1 || c==escapeChar)
      { out.append(escapeChar);
      }
      out.append(c);
    }
    return out.toString();
  }
  
  public static String unescape(String input,char escapeChar)
  {
    final StringBuilder out=new StringBuilder();
    boolean escapeNext=false;
    for (char c:input.toCharArray())
    {
      if (c==escapeChar && !escapeNext)
      { escapeNext=true;
      }
      else
      {
        out.append(c);
        escapeNext=false;
      }
    }
    return out.toString();
   
  }
  
}
