package spiralcraft.text.markup;

import spiralcraft.text.ParseException;
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
  private final int _startDelimiterLength;
  private final int _endDelimiterLength;
  private final CharSequence _endDelimiter;
  
  public MarkupParser(CharSequence startDelimiter,CharSequence endDelimiter)
  { 
    _endDelimiter=endDelimiter;
    _beginTagMatcher=new KmpMatcher(startDelimiter);
    _startDelimiterLength=startDelimiter.length();
    _endTagMatcher=new KmpMatcher(endDelimiter);  
    _endDelimiterLength=endDelimiter.length();
  }
  
  /**
   * Supply a ContentHandler for the parser to process
   *   the text and code fragments read from the input.
   */
  public void setMarkupHandler(MarkupHandler val)
  { _markupHandler=val;
  }
  
  public void parse(CharSequence sequence)
    throws ParseException
  {
    boolean inText=true;
    _beginTagMatcher.reset();
    _endTagMatcher.reset();
    int mark=0;
    for (int i=0;i<sequence.length();i++)
    {
      try
      {
        if (inText)
        { 
          if (_beginTagMatcher.match(sequence.charAt(i)))
          { 
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
      catch (Exception x)
      {
        throw new ParseException
          ("Error handling markup",i,x);
      }
    }
    if(!inText)
    { 
      throw new ParseException
        ("Unexpected end of input. Missing "
        +_endDelimiter
        ,sequence.length()
        );
    }
    _markupHandler.handleContent(sequence.subSequence(mark,sequence.length()));
  }
}