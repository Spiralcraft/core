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
package spiralcraft.text.html;

import java.io.IOException;
import spiralcraft.text.Encoder;

public class HtmlEncoder
{

  public static final char AMP_C='&';
  public static final char GT_C='>';
  public static final char LT_C='<';
  public static final char BR_C='\n';
  public static final char CR_C='\r';
  public static final char TAB_C='\t';

  public static final String AMP_S="&amp;";
  public static final String GT_S="&gt;";
  public static final String LT_S="&lt;";
  public static final String BR_S="<BR>";
  public static final String TAB_S="&nbsp;&nbsp;&nbsp;&nbsp;";

  public static final Encoder encoder()
  { 
    return new Encoder() 
    {
      
  
      @Override
      public Appendable encode(
        CharSequence in,
        Appendable out)
        throws IOException
      {
        if (out==null && in!=null)
        { out=new StringBuilder(HtmlEncoder.encode(in.toString()));
        }
        else if (in!=null)
        { out.append(HtmlEncoder.encode(in.toString()));
        }
        return out;
      }
    };
  }
  
  public static String encode(String s)
  {
  	if (s==null)
  	{ return null;
  	}
    char[] c=s.toCharArray();
    StringBuffer out=new StringBuffer();
    boolean inCR=false;
    boolean inSP=false;
    for (int i=0;i<c.length;i++)
    {
      if (inCR)
      { 
        out.append(BR_S);
        if (c[i] == CR_C)
        { continue;
        }
        else if (c[i] == BR_C)
        { 
          inCR=false;
          continue;
        }
        else
        { inCR=false;
        }
      }
      else if (c[i] == CR_C)
      { 
        inCR=true;
        inSP=false;
        continue;
      }
      else if (c[i] == BR_C)
      { 
        inSP=false;
        out.append(BR_S);
        continue;
      }
      
      if (c[i]==' ')
      { 
        if (inSP)
        { out.append("&nbsp;");
        }
        else
        { 
          out.append(" ");
          inSP=true;
        }
        continue;
      }
      else
      { inSP=false;
      }
        
      String enc=encodeChar(c[i]);
      if (enc!=null)
      { out.append(enc);
      }
      else
      { out.append(c[i]);
      }
    }
    if (inCR)
    { out.append(BR_S);
    }

    return out.toString();

  }

  public static String encodeChar(char c)
  {
    switch (c)
    {
    case AMP_C:
      return AMP_S;
    case GT_C:
      return GT_S;
    case LT_C:
      return LT_S;
    case TAB_C:
      return TAB_S;
    default:
      return null;
    }

  }


}
