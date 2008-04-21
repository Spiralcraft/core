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

public class TextAreaEncoder
{

  public static final char AMP_C='&';
  public static final char GT_C='>';
  public static final char LT_C='<';
  public static final char BR_C='\n';

  public static final String AMP_S="&amp;";
  public static final String GT_S="&gt;";
  public static final String LT_S="&lt;";
  public static final String BR_S="<BR>";


  public static String encode(String s)
  {
  	if (s==null)
  	{ return null;
  	}
    char[] c=s.toCharArray();
    StringBuffer out=new StringBuffer();
    for (int i=0;i<c.length;i++)
    {
      String enc=encodeChar(c[i]);
      if (enc!=null)
      { out.append(enc);
      }
      else
      { out.append(c[i]);
      }
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
    default:
      return null;
    }

  }


}
