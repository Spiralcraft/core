package spiralcraft.text.html;

import spiralcraft.text.Encoder;

import java.io.Writer;

import java.io.IOException;

/**
 * <p>Encodes arbitrary text for inclusion in a quoted attribute value inside
 *   an HTML tag
 * </p>
 * 
 * <p>Specifically, all characters with values < 32 are encoded using a 
 *   numeric entity, and the '<', '>', '"' and '&' chars are encoded using
 *   named entities.
 * </p>
 * @author mike
 *
 */
public class AttributeEncoder
  implements Encoder
{
  private static final char AMP_C='&';
  private static final String AMP_S="&amp;";
  
  private static final char GT_C='>';
  private static final String GT_S="&gt;";

  private static final char LT_C='<';
  private static final String LT_S="&lt;";

  @Override
  public void encode(CharSequence in,Writer out)
    throws IOException
  {
    if (in==null)
    { return;
    }
    for (int i=0;i<in.length();i++)
    { 
      char c=in.charAt(i);
      if (c < ' ')
      { out.write("&#"+((int) c)+";"); 
      }
      else
      {
        switch (c)
        {
        case AMP_C:
          out.write(AMP_S);
        case GT_C:
          out.write(GT_S);
        case LT_C:
          out.write(LT_S);
        case '"':
          out.write("&quot;");
        default:
          out.write(c);
        }
      }
      
      
    }
  }
}
