package spiralcraft.text.markup;


/**
 * A Unit which represents markup. May contain other Units if the markup
 *   language supports containership. This is a base class from which 
 *   language specific MarkupUnits are derived.
 */
public class MarkupUnit
  extends Unit
{
  
  private final CharSequence _code;
  private boolean _open=true;
  
  public MarkupUnit(CharSequence code)
    throws MarkupException
  { _code=code;
  }
  
  
  public boolean isOpen()
  { return _open;
  }

  public void close()
    throws MarkupException
  { _open=false;
  }


  public String toString()
  { return super.toString()+"[name="+getName()+"]";
  }

}
