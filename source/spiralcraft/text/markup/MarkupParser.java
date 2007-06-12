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
package spiralcraft.text.markup;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;
import spiralcraft.text.KmpMatcher;

/**
 * Parser for generic markup. Given delimiters for the markup, divides input
 *   into units of text and markup. 
 */
public class MarkupParser
{
  private MarkupHandler _markupHandler;
  private final KmpMatcher _beginTagMatcher;
  private final KmpMatcher _endTagMatcher;
  private final KmpMatcher _lineMatcher;
  private final int _startDelimiterLength;
  private final int _endDelimiterLength;
  private final CharSequence _endDelimiter;
  private final ParsePosition position=new ParsePosition();
  
  public MarkupParser(CharSequence startDelimiter,CharSequence endDelimiter)
  { 
    _endDelimiter=endDelimiter;
    _beginTagMatcher=new KmpMatcher(startDelimiter);
    _startDelimiterLength=startDelimiter.length();
    _endTagMatcher=new KmpMatcher(endDelimiter);  
    _endDelimiterLength=endDelimiter.length();
    _lineMatcher=new KmpMatcher(System.getProperty("line.separator"));
  }
  
  /**
   * Supply a ContentHandler for the parser to process
   *   the text and code fragments read from the input.
   */
  public void setMarkupHandler(MarkupHandler val)
  { _markupHandler=val;
  }
  
  public void parse(CharSequence sequence)
    throws ParseException,MarkupException
  {
    boolean inText=true;
    _beginTagMatcher.reset();
    _endTagMatcher.reset();
    _lineMatcher.reset();
    position.setLine(1);
    int mark=0;
    for (int i=0;i<sequence.length();i++)
    {
      position.incIndex(1);
      position.incColumn(1);
      if (_lineMatcher.match(sequence.charAt(i)))
      { 
        position.incLine(1);
        position.setColumn(1);
      
      }

      if (inText)
      { 
        if (_beginTagMatcher.match(sequence.charAt(i)))
        { 
          _markupHandler.setPosition(position);
          _markupHandler.handleContent
          (sequence.subSequence
              (mark
                  ,i-(_startDelimiterLength-1)
              )
          );
          mark=i+1;
          inText=false;
        }
      }
      else
      {
        if (_endTagMatcher.match(sequence.charAt(i)))
        { 
          _markupHandler.setPosition(position);
          _markupHandler.handleMarkup
          (sequence.subSequence
              (mark
                  ,i-(_endDelimiterLength-1)
              )
          );
          mark=i+1;
          inText=true;
        }
      }
    }
    if (!inText)
    { 
      throw new ParseException
        ("Unexpected end of input. Missing "
        +_endDelimiter
        ,position
        );
    }
    _markupHandler.setPosition(position);
    _markupHandler.handleContent
      (sequence.subSequence(mark,sequence.length()));
  }
}