package spiralcraft.text.markup;

import spiralcraft.text.Trimmer;
import spiralcraft.text.ParseException;


/**
 * Compiles a CharSequence containing markup into a tree of Units. The
 *   actual interpretation of the markup is left to the subclass.
 */
public abstract class MarkupCompiler
  implements MarkupHandler
{
  
  private final MarkupParser _parser;
  private final Trimmer _trimmer=new Trimmer("\r\n\t ");
  private final CharSequence _startDelimiter;
  private final CharSequence _endDelimiter;
  private Unit _unit;
  
  
  public MarkupCompiler
    (CharSequence startDelimiter
    ,CharSequence endDelimiter
    )
  { 
    _startDelimiter=startDelimiter;
    _endDelimiter=endDelimiter;
    _parser=new MarkupParser(startDelimiter,endDelimiter);
    _parser.setMarkupHandler(this);
  }

  public synchronized CompilationUnit compile(CharSequence sequence)
    throws ParseException
  { 
    _unit=createCompilationUnit();

    _parser.parse(sequence);
    
    if (!(_unit instanceof CompilationUnit))
    { 
      throw new ParseException
        ("Unexpected end of input. Unclosed unit "+_unit.getName()
        ,sequence.length()
        );
    }
    return (CompilationUnit)  _unit;
  }

  public void handleContent(CharSequence text)
  { _unit.addChild(new ContentUnit(text));
  }
  
  /**
   * Closes the current containing unit
   */
  protected final void closeUnit()
    throws MarkupException
  { 
    _unit.close();
    _unit=_unit.getParent();
  }
  
  /**
   * Obtain the current containing unit
   */
  protected final Unit getUnit()
  { return _unit;
  }
  
  protected final void addUnit(Unit newUnit)
  {
    _unit.addChild(newUnit);
    if (newUnit.isOpen())
    { _unit=newUnit;
    }
  }
  
  protected abstract CompilationUnit createCompilationUnit();
  
  public abstract void handleMarkup(CharSequence code)
    throws Exception;

}
