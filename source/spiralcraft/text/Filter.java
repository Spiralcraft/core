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
package spiralcraft.text;

import java.util.HashMap;

/**
 * Filters text from CharSequences 
 */
public class Filter
{
  private final String _chars;
  
  /**
   * Construct a Filter which filters the specified chars
   */
  public Filter(String chars)
  { _chars=chars;
  }

  /**
   * Filter characters from the given CharSequence
   */
  public String filter(CharSequence original)
  {
    char[] out=new char[original.length()];
    int pos=0;
    for (int i=0;i<original.length();i++)
    { 
      char ch=original.charAt(i);
      if (_chars.indexOf(ch)==-1)
      { out[pos++]=ch;
      }
    }
    return new String(out,0,pos);
  }

}
