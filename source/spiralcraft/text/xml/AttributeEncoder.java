package spiralcraft.text.xml;

import spiralcraft.text.Encoder;

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

  public String encode(CharSequence in)
    throws IOException
  {
    StringBuilder builder=new StringBuilder();
    return encode(in,builder).toString();
  } 
  
  @Override
  public Appendable encode(CharSequence in,Appendable out)
    throws IOException
  {
    if (in==null)
    { return out;
    }
    for (int i=0;i<in.length();i++)
    { 
      char c=in.charAt(i);
      if (c < ' ')
      { out.append("&#"+((int) c)+";"); 
      }
      else
      {
        switch (c)
        {
        case AMP_C:
          out.append(AMP_S);
          break;
        case GT_C:
          out.append(GT_S);
          break;
        case LT_C:
          out.append(LT_S);
          break;
        case '"':
          out.append("&quot;");
          break;
        default:
          out.append(c);
        }
      }
      
      
    }

    return out;
  }
}
