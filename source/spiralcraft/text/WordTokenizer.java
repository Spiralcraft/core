//
// Copyright (c) 2013 Michael Toth
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

import java.util.ArrayList;

/**
 * Tokenize input on word boundaries for non-grammatical scenarios using
 *   unicode attributes.
 * 
 * @author mike
 *
 */
public class WordTokenizer
{

  static enum CharClass
  { 
    ALPHANUM
    ,QUOT
    ,PUNC
    ,WHITESPACE
    ,NONE
  };
  
  private boolean includeWhitespace;
  private boolean includePunctuation;
  private boolean includeQuotes;
  private boolean ignoreQuotes;
  private boolean lossless;
  
  public void setIncludeWhitespace(boolean includeWhitespace)
  { this.includeWhitespace=includeWhitespace;
  } 
  
  public void setIncludePunctuation(boolean includePunctuation)
  { this.includePunctuation=includePunctuation;
  } 

  public void setIncludeQuotes(boolean includeQuotes)
  { this.includeQuotes=includeQuotes;
  } 
  
  private boolean includeWhitespace()
  { return includeWhitespace || lossless;
  }

  private boolean includeQuotes()
  { return includeQuotes || lossless;
  }

  private boolean includePunctuation()
  { return includePunctuation || lossless;
  }

  /** 
   * Don't lose any information when tokenizing. Will return whitespace,
   *   punctuation and quotes as separate tokens.
   *  
   * @param lossless
   */
  public void setLossless(boolean lossless)
  { this.lossless=lossless;
  }
  
  public void setIgnoreQuotes(boolean ignoreQuotes)
  { this.ignoreQuotes=ignoreQuotes;
  }

  public String[] tokenize(String input)
  {
    
    StringBuilder word=new StringBuilder();
    ArrayList<String> tokens=new ArrayList<String>();
    CharClass lastCC=CharClass.NONE;
    CharClass newCC=CharClass.NONE;
    for (char chr:input.toCharArray())
    {
      newCC=
        Character.isLetterOrDigit(chr)
        ?CharClass.ALPHANUM
        :(chr=='\'' || chr=='"' || chr=='`')
        ?CharClass.QUOT
        :Character.isWhitespace(chr)
        ?CharClass.WHITESPACE
        :CharClass.PUNC
        ;
      
      if (newCC==CharClass.QUOT && ignoreQuotes)
      { newCC=CharClass.ALPHANUM;
      }
      
      if (newCC!=lastCC || newCC==CharClass.PUNC)
      { 
        if (word.length()>0)
        { 
          tokens.add(word.toString());
          word.setLength(0);
        }
      }
      
      
      if ( !(newCC==CharClass.WHITESPACE && !includeWhitespace()
            || newCC==CharClass.PUNC && !includePunctuation()
            || newCC==CharClass.QUOT && !includeQuotes()
            )
         )
      { word.append(chr);
      }
      
      lastCC=newCC;
      
    }
    if (word.length()>0)
    {
      tokens.add(word.toString());
      word.setLength(0);
    }
    return tokens.toArray(new String[tokens.size()]);
  }
}
