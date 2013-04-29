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

import spiralcraft.codec.text.HexCodec;

/**
 * Translate between byte[] and base16 (hexadecimal) text representation
 * 
 * @author mike
 *
 */
public class ByteArrayToHex
  extends StringConverter<byte[]>
{
  private static final ByteArrayToHex INSTANCE
    =new ByteArrayToHex();
  
  public static final ByteArrayToHex instance()
  { return INSTANCE;
  }
    
  @Override
  public byte[] fromString(String val)
  { 
    if (val==null || val.isEmpty())
    { return null;
    }
    return HexCodec.decodeHex(val);
  }
  
  @Override
  public String toString(byte[] val)
  { 
    if (val==null || val.length==0)
    { return null;
    }
    return HexCodec.encodeHex(val);
  }
  
}