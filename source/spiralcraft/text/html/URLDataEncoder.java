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

import java.io.IOException;
import java.nio.charset.Charset;

import spiralcraft.text.Codec;
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
  
  public static final Codec codec()
  { return codec("UTF-8");
  }
  
  public static final Codec codec(final String charsetName)
  { 
    return new Codec() 
    {
  
      private final Charset charset=Charset.forName(charsetName);
      
      
      @Override
      public Appendable decode(
        CharSequence in,
        Appendable out)
        throws IOException
      {
        if (out==null && in!=null)
        { out=new StringBuilder(URLDataEncoder.decode(in.toString(),charset));
        }
        else if (in!=null)
        { out.append(URLDataEncoder.decode(in.toString(),charset));
        }
        return out;
      }
  
      @Override
      public Appendable encode(
        CharSequence in,
        Appendable out)
        throws IOException
      {
        if (out==null && in!=null)
        { out=new StringBuilder(URLDataEncoder.encode(in.toString(),charset));
        }
        else if (in!=null)
        { out.append(URLDataEncoder.encode(in.toString(),charset));
        }
        return out;
      }
    };
  }
 

  public static String encode(String plaintext)
  { return encode(plaintext,UTF_8);
  }
  
  public static String encode(String plaintext,Charset encoding)
  {
    StringBuilder encoded = new StringBuilder();
    char[] chars = plaintext.toCharArray();
    for (int i=0; i<chars.length; i++)
    {
      char c = chars[i];
      if (c==' ')
      { encoded.append('+');
      }
      else if ( ! ((c>='A' && c<='Z') || 
              (c>='a' && c<='z') || 
              (c>='0' && c<='9') ||
              ("$-_.!*'()".indexOf(c)>-1)
              ))
      {
        
        byte[] bytes
          =Character.toString(c).getBytes(encoding!=null?encoding:UTF_8);
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