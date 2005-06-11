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
 *
 * Implements the Knuth-Morris-Pratt algorithm to detect
 *   a pattern match in a string of characters.
 *  
 */
public class KmpMatcher
{

  private final char[] _match;
  private final int[] _map;
  private int _matchPos=-1;

  int[] getMap()
  { return _map;
  }

  /**
   * Construct a new KmpMatcher which 
   *   matches the specified CharSequence
   */
  public KmpMatcher(CharSequence toMatch)
  { 
    _match=new char[toMatch.length()];
    for (int i=0;i<toMatch.length();i++)
    { _match[i]=toMatch.charAt(i);
    }
    _map=new int[_match.length];
    makeMap();    
   
  }

  /**
   * Reset the matcher in preparation for new input.
   */
  public void reset()
  { _matchPos=-1;
  }
  
  /**
   * Match the match string with the text seen so far.
   *
   *@return Whether the supplied character completes
   *  a match.
   */
  public final boolean match(final char c)
  {
    boolean inBeginning=_matchPos==-1;
    _matchPos++;

    while (c!=_match[_matchPos])
    {
      _matchPos=_map[_matchPos];
      if (_matchPos==-1)
      {
        if (!inBeginning)
        {
          inBeginning=true;
          _matchPos=0;
        }
        else
        { break;
        }
      }
        
    }
    if (_matchPos==_match.length-1)
    {
      _matchPos=_map[_matchPos];
      while (_matchPos>=0 && c!=_match[_matchPos])
      { _matchPos=_map[_matchPos];
      }
      return true;
    }
    return false;

  }

  /**
   * Map the match string onto itself to create a
   *   state-transition model.
   */
  private final void makeMap()
  {

    _matchPos=-1;
    _map[0]=-1;

    int i=0;
    int j=-1;
    while (true)
    {
      while ( (j>=0) && (_match[i]!=_match[j]))
      { j=_map[j];
      }

      i++;
      j++;
      if (i>=_match.length)
      { break;
      }

      _map[i]=(_match[i]!=_match[j]?_map[j]:j);
    }
  }


}
