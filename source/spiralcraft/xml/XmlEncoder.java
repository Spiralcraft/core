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
package spiralcraft.xml;

import spiralcraft.text.Encoder;

import java.io.Writer;
import java.io.IOException;

/**
 * An encoder which encodes a CharSequence for 
 *   embedding anywhere within an XML document
 */
public class XmlEncoder
  implements Encoder
{
  public void encode(CharSequence in,Writer out)
    throws IOException
  {
    for (int i=0;i<in.length();i++)
    { 
      final char value=in.charAt(i);
      switch (value)
      { 
        case '&':
          out.write("&amp;");
          break;
        case '<':
          out.write("&lt;");
          break;
        case '>':
          out.write("&gt;");
          break;
        default:
          out.write(value);
          break;
      }
    }

  }
  
}
