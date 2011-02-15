//
//Copyright (c) 1998,2007 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.text.html;

import java.nio.charset.Charset;

import spiralcraft.util.ByteBuffer;


/**
 * <p>Encodes and decodes URLEncoded text for escaping arbitrary character
 *   data for embedding in URLs.
 * </p>
 * 
 * <p>This encoder is suitable for embedding data in individual values
 *   within a query string or a post of application/x-www-form-urlencoded 
 *   content type.
 * </p>
 *   
 * 
 * <p>Specifically, groups [A-Z], [a-z], and [0-9] are preserved, spaces are
 *   replaced with '+', and all other characters, including '+', are 
 *   numerically escaped using the %nn hex encoding method.
 * </p>
 * 
 * @author mike
 *
 */
public class URLDataEncoder
{  
  private static final Charset UTF_8=Charset.forName("UTF-8");
  
  public static String encode(String plaintext)
  { return encode(plaintext,UTF_8);
  }
  
  public static String encode(String plaintext,Charset encoding)
  {
    StringBuilder encoded = new StringBuilder();
    byte[] bytes = plaintext.getBytes(encoding!=null?encoding:UTF_8);
    for (int i=0; i<bytes.length; i++)
    {
      byte c = bytes[i];
      if (c==' ')
      { encoded.append('+');
      }
      else if ( ! ((c>='A' && c<='Z') || 
              (c>='a' && c<='z') || 
              (c>='0' && c<='9') ||
              ("$-_.!*'()".indexOf(c)>-1)
              ))
      {
        encoded.append('%');
        String hex=(Integer.toHexString(c));
        if (hex.length()==1)       
        { encoded.append("0");
        }
        encoded.append(hex);
      }
      else
      { encoded.append(c);
      }

    }
    return encoded.toString();
  }
  
  public static String decode(String encodedText)
  { return decode(encodedText,UTF_8);
  } 
  
  /**
   * Decode a URLEncoded string. Return null if
   *   there was a format problem.
   */
  public static String decode(String encodedText,Charset encoding)
  {
    // System.out.println("encoded Text: "+encodedText);
    ByteBuffer decoded = new ByteBuffer();
    char[] chars = encodedText.toCharArray();
    for (int i=0; i<chars.length; i++)
    {
      char c = chars[i];
      if (c == '+')
      { decoded.append(' ');
      }
      else if (c == '%')
      {
        try
        {
          // convert hexvalue to character
          char[] hex={chars[++i],chars[++i]};
          decoded.append( (char) Integer.valueOf(new String(hex), 16).intValue());                  
        }
        catch (Exception x)
        { return null;
        }
        
      }
      else
      { decoded.append(c);
      }
    }
    //System.out.println("plaintext: "+decoded.toString());
    return new String(decoded.toByteArray(),encoding!=null?encoding:UTF_8);
  }

}