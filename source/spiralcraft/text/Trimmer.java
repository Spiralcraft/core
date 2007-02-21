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

/**
 * Trims text from CharSequences 
 */
public class Trimmer
{
  private final String _chars;
  
  /**
   * Construct a trimmer which trims the specified chars
   */
  public Trimmer(String chars)
  { _chars=chars;
  }

  /**
   * Trim text from both ends of the given CharSequence
   */
  public CharSequence trim(CharSequence original)
  {
    final int length=original.length();
    int start=0;
    int end=length;

    while (start<length
          && _chars.indexOf(original.charAt(start))>-1
          )
    { start++;
    }

    while (end>0
          && _chars.indexOf(original.charAt(end-1))>-1
          )
    { end--;
    }
    
    return original.subSequence(start,end);
  }

}
