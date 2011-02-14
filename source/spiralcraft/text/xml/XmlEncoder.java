//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.text.xml;

import spiralcraft.log.ClassLog;
import spiralcraft.text.Encoder;

import java.io.IOException;

/**
 * <p>An encoder which encodes a CharSequence for embedding 
 *      within an XML document
 * </p>
 */
public class XmlEncoder
  implements Encoder
{
  private static final ClassLog log
    =ClassLog.getInstance(XmlEncoder.class);
  
  public void encodeRaw(CharSequence in,Appendable out)
    throws IOException
  {
    for (int i=0;i<in.length();i++)
    { 
      final char value=in.charAt(i);
      if (value < ' ')
      { 
        switch (value)
        {
          case '\r':
          case '\n':
          case '\t':
            out.append(value);
          break;
        default:
          log.warning
            ("Non-encodable character "+((int) value)+" ignored");
          // out.write("&#"+((int) value)+";"); 
        }
      }      
      else if (value >= 0x7F && value <=0x9F)
      { out.append("&#"+((int) value)+";");
      }
      else switch (value)
      { 
        case '&':
          out.append("&amp;");
          break;
        case '<':
          out.append("&lt;");
          break;
        case '>':
          out.append("&gt;");
          break;
        default:
          out.append(value);
          break;
      }
    }
  }

  /**
   * <p>Escapes all control characters and XML entities, including whitespace.
   * </p>
   * 
   * <p>Whitespace is preserved in data.
   * </p> 
   */
  @Override
  public void encode(CharSequence in,Appendable out)
    throws IOException
  {
    for (int i=0;i<in.length();i++)
    { 
      final char value=in.charAt(i);
      if (value < ' ')
      { out.append("&#"+((int) value)+";"); 
      }      
      else switch (value)
      { 
        case '&':
          out.append("&amp;");
          break;
        case '<':
          out.append("&lt;");
          break;
        case '>':
          out.append("&gt;");
          break;
        default:
          out.append(value);
          break;
      }
    }

  }
  
}
