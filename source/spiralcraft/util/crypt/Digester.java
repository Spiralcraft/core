//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.util.crypt;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import spiralcraft.codec.text.HexCodec;

public class Digester
{
  private final MessageDigest messageDigest;
  
  public Digester(String algorithm)
  {
    try
    { messageDigest=MessageDigest.getInstance(algorithm);
    }
    catch (NoSuchAlgorithmException x)
    { throw new RuntimeException(x);
    }
  }
  
  public String digestToHex(String input)
  { 
    try
    {
      byte[] digestBytes=messageDigest.digest(input.getBytes("UTF-8"));
      return HexCodec.encodeHex(digestBytes);
    }
    catch (UnsupportedEncodingException x)
    { throw new RuntimeException("UTF-8 encoding not supported");
    }
  }
  

}
