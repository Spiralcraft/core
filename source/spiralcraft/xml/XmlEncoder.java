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
