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
package spiralcraft.codec.text;


public class HexCodec
{

  private static final char[] encoding = 
    {'0','1','2','3','4','5','6','7'
    ,'8','9','a','b','c','d','e','f'
    };
  

  public static final byte[] decodeHex(String input)
  {
    if (input==null)
    { return null;
    }
    
    int len = input.length();
    if ( (len/2) * 2 != len)
    { 
      throw new NumberFormatException
        ("Hexadecimal number must have an even number of digits");
    }
    byte[] data = new byte[len/2];
    for (int i=0; i< len; i+=2) 
    {
      int d1=Character.digit(input.charAt(i),16);
      int d2=Character.digit(input.charAt(i+1),16);
      if (d1<0)
      { 
        throw new NumberFormatException
          (input.charAt(i)+" is not a hexadecimal digit");
      }
      if (d2<0)
      { 
        throw new NumberFormatException
          (input.charAt(i+1)+" is not a hexadecimal digit");
      }
      
      
      data[i/2] = (byte) ( (d1 << 4) + d2 );
    }
    return data;
  }
    
  public static final String encodeHex(byte[] input)
  {
    int len=input.length;
    char[] data=new char[len*2];
    for (int i=0;i<len;i++)
    { 
      int ubyte=input[i] & 0xFF;
        
      data[i*2] = encoding[ubyte >> 4];
      data[(i*2)+1] = encoding[ubyte & 0x0f];
    }
    return String.copyValueOf(data);
  }
    
}
