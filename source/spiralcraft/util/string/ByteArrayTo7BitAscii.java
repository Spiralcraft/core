//
// Copyright (c) 2010 Michael Toth
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

import java.nio.charset.Charset;


/**
 * Translate between byte[] and safe ASCII (7 bit, no control chars)
 *   representation
 * 
 * @author mike
 *
 */
public class ByteArrayTo7BitAscii
  extends StringConverter<byte[]>
{
  private static final Charset ASCII=Charset.forName("US-ASCII");
  
  @Override
  public byte[] fromString(String val)
  { 
    if (val==null || val.isEmpty())
    { return null;
    }
    return val.getBytes(ASCII);
  }
  
  @Override
  public String toString(byte[] val)
  { 
    if (val==null || val.length==0)
    { return null;
    }
    char[] chars=new char[val.length];
    int i=0;
    for (byte b: val)
    {
      int value=(int) b & 0xFF;
      if (value<32 || value>126)
      { chars[i]='?';
      }
      else
      { chars[i]=(char) value;
      }
      i++;
    }
    return String.valueOf(chars);
  }
  
}