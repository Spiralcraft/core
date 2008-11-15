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
  private MarkupHandler handler;
  private final KmpMatcher _beginTagMatcher;
  private final KmpMatcher _endTagMatcher;
  private final KmpMatcher _lineMatcher;
  private final int _startDelimiterLength;
  private final int _endDelimiterLength;
  private final CharSequence _endDelimiter;
  private final CharSequence _startDelimiter;
  private char _escapeChar;
  private ParsePosition position;
  
  public MarkupParser
    (CharSequence startDelimiter
    ,CharSequence endDelimiter
    ,char escapeChar
    )
  { 
    this(startDelimiter,endDelimiter);
    _escapeChar=escapeChar;
  }
    
  public MarkupParser(CharSequence startDelimiter,CharSequence endDelimiter)
  { 
    _startDelimiter=startDelimiter;
    _endDelimiter=endDelimiter;
    _beginTagMatcher=new KmpMatcher(startDelimiter);
    _startDelimiterLength=startDelimiter.length();
    _endTagMatcher=new KmpMatcher(endDelimiter);  
    _endDelimiterLength=endDelimiter.length();
    _lineMatcher=new KmpMatcher(System.getProperty("line.separator"));
  }
  
  public void setPosition(ParsePosition position)
  { this.position=position;
  }
  
  /**
   * Supply a ContentHandler for the parser to process
   *   the text and code fragments read from the input.
   */
  public void setMarkupHandler(MarkupHandler val)
  { handler=val;
  }
  
  public void parse(CharSequence sequence)
    throws ParseException,MarkupException
  { parse(sequence,handler,position);
  }

  public void parse
    (CharSequence sequence
    ,MarkupHandler handler
    ,ParsePosition position
    )
    throws ParseException,MarkupException
  {
    if (position==null)
    { position=new ParsePosition();
    }
    this.position=position;
    boolean inText=true;
    boolean inEscape=false;
    _beginTagMatcher.reset();
    _endTagMatcher.reset();
    _lineMatcher.reset();
    position.setLine(1);
    int mark=0;
    for (int i=0;i<sequence.length();i++)
    {
      char chr=sequence.charAt(i);
      position.incIndex(1);
      position.incColumn(1);
      if (_lineMatcher.match(chr))
      { 
        position.incLine(1);
        position.setColumn(1);
      
      }

      if (inText)
      { 
        if (!inEscape && chr==_escapeChar)
        { inEscape=true;
        }
        else if (inEscape)
        {
          if (chr==_escapeChar
              || chr==_startDelimiter.charAt(0)
             )
          { 
            // Drop the escape char and queue the current position
            handler.setPosition(position);
            handler.handleContent
              (sequence.subSequence
                (mark
                ,i-1
                )
              );
            mark=i;
          }
          inEscape=false;
        }
        else if (_beginTagMatcher.match(chr))
        { 
          handler.setPosition(position);
          handler.handleContent
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
        if (_endTagMatcher.match(chr))
        { 
          handler.setPosition(position);
          handler.handleMarkup
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
    handler.setPosition(position);
    handler.handleContent
      (sequence.subSequence(mark,sequence.length()));
  }
}