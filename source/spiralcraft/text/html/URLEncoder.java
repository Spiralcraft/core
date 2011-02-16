//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.text.html;

import java.nio.charset.Charset;

/**
 * Encodes a hierarchical path 
 * 
 * TODO: Rename this class to URLPathEncoder
 * 
 * @author mike
 *
 */
public class URLEncoder
{

  private static final Charset UTF_8=Charset.forName("UTF-8");
  
  public static final char AMP_C='&';
  public static final char GT_C='>';
  public static final char LT_C='<';
  public static final char BR_C='\n';

  public static final String AMP_S="&amp;";
  public static final String GT_S="&gt;";
  public static final String LT_S="&lt;";
  public static final String BR_S="<BR>";


  /**
   * Translate an encoded URL path by removing "%nn" hex encoded 
   *   characters
   * 
   * @param encodedText
   * @return The decoded string
   */
  public static String decode(String encodedText)
  {
    StringBuffer decoded = new StringBuffer();
    char[] chars = encodedText.toCharArray();
    for (int i=0; i<chars.length; i++)
    {
      char c = chars[i];
      if (c == '%')
      {
        try
        {
          // convert hexvalue to character
          char[] hex={chars[++i],chars[++i]};
          int value=Integer.valueOf(new String(hex), 16).intValue();
          decoded.append( (char) value);       
        }
        catch (Exception x)
        { return null;
        }
        
      }
      else
      { decoded.append(c);
      }
    }
    return decoded.toString();
  }

  /**
   * <p>Encode a path string for inclusion in the path segment of a URL.
   * </p>
   * 
   * <p>Preserve path characters '/' and ';' and all reserved inclusions of 
   *   the rfc2396 pchar set ':','@','&','=','+','$',','. 
   *   
   */
  public static String encode(String urlNoQuery)
  {
    if (urlNoQuery==null)
    { return "";
    }
    StringBuffer encoded = new StringBuffer();
    char[] chars = urlNoQuery.toCharArray();
    for (int i=0; i<chars.length; i++)
    {
      char c = chars[i];
      switch (c)
      {
      case '-':
      case '_':
      case '.':
      case '!':
      case '~':
      case '*':
      case '\'':
      case '(':
      case ')':
      case '/':
      case ';':
      case ':':
      case '@':
      case '&':
      case '=':
      case '+':
      case '$':
      case ',':
        encoded.append(c);
        break;
      default:
        if ((c>='A' && c<='Z') || (c>='a' && c<='z') || (c>='0' && c<='9'))
        { encoded.append(c);
        }
        else
        {
          byte[] bytes
            =Character.toString(c).getBytes(UTF_8);
          for (byte b:bytes)
          {
            encoded.append('%');
            String hex=(Integer.toHexString(b & 0xFF)).toUpperCase();
            if (hex.length()==1)       
            { encoded.append("0");
            }
            encoded.append(hex);
          
          }          
        }
      }

    }
    return encoded.toString();
  }

  public static String encodeForTextArea(String s)
  {
  	if (s==null)
  	{ return null;
  	}
    char[] c=s.toCharArray();
    StringBuffer out=new StringBuffer();
    for (int i=0;i<c.length;i++)
    {
      String enc=encodeTextAreaChar(c[i]);
      if (enc!=null)
      { out.append(enc);
      }
      else
      { out.append(c[i]);
      }
    }
    return out.toString();

  }

  public static String encodeTextAreaChar(char c)
  {
    switch (c)
    {
    case AMP_C:
      return AMP_S;
    case GT_C:
      return GT_S;
    case LT_C:
      return LT_S;
    default:
      return null;
    }

  }

  public static String encodeString(String s)
  {
  	if (s==null)
  	{ return null;
  	}
    char[] c=s.toCharArray();
    StringBuffer out=new StringBuffer();
    for (int i=0;i<c.length;i++)
    {
      String enc=encodeChar(c[i]);
      if (enc!=null)
      { out.append(enc);
      }
      else
      { out.append(c[i]);
      }
    }
    return out.toString();
  }

  public static String encodeChar(char c)
  {
    switch (c)
    {
    case AMP_C:
      return AMP_S;
    case GT_C:
      return GT_S;
    case LT_C:
      return LT_S;
    case BR_C:
      return BR_S;
    default:
      return null;
    }

  }
}
