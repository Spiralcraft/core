//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.util;


/**
 * Accepts a continuous series of bytes and
 *   indicates the state of an ongoing match to a
 *   specified byte pattern.
 *
 * Implements the Knuth-Morris-Pratt algorithm for
 *   efficiency.
 *  
 */
public class BytePatternMatcher
{

  private final byte[] pattern;
  private final int[] kmpMap;
  private int matchPos=-1;



  /**
   * Construct a new byte pattern detector with
   *   the specified bytes to match.
   */
  public BytePatternMatcher(byte[] toMatch)
  { 
    this.pattern=toMatch;
    kmpMap=new int[pattern.length];
    buildMap();
  }

  /**
   * Position the matcher to the beginning of
   *   the pattern.
   */
  public final void reset()
  { matchPos=-1;
  }



  /**
   * Return the index in the pattern where
   *   a match currently exists. If no match
   *   exists, this will be -1. If a match
   *   was just made, this will be reset to
   *   -1, or a smaller index of the pattern
   *   repeats itself.
   */
  public int getMatchPos()
  { return matchPos;
  }

  public boolean isMatch()
  { return matchPos==pattern.length-1;
  }

  /**
   * Match the match string with the text seen so far.
   *@return Whether a match has just occured.
   */
  public final boolean match(byte c)
  {
    boolean inBeginning=matchPos==-1;
    matchPos++;

    while (c!=pattern[matchPos])
    {
      matchPos=kmpMap[matchPos];
      if (matchPos==-1)
      {
        if (!inBeginning)
        {
          inBeginning=true;
          matchPos=0;
        }
        else
        { break;
        }
      }
    }
    if (matchPos==pattern.length-1)
    {
      matchPos=kmpMap[matchPos];
      while (matchPos>=0 && c!=pattern[matchPos])
      { matchPos=kmpMap[matchPos];
      }
      return true;
    }
    return false;

  }

  /**
   * Map the match string onto itself to create a
   *   state-transition diagram.
   */
  private final void buildMap()
  {

    matchPos=-1;
    kmpMap[0]=-1;

    int i=0;
    int j=-1;
    while (true)
    {
      while ( (j>=0) && (pattern[i]!=pattern[j]))
      { j=kmpMap[j];
      }

      i++;
      j++;
      if (i>=pattern.length)
      { break;
      }

      kmpMap[i]=(pattern[i]!=pattern[j]?kmpMap[j]:j);
    }
  }


}
