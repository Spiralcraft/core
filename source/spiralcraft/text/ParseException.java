package spiralcraft.text;

/**
 * A generic exception parsing text
 */
public class ParseException
  extends Exception
{
  private int _offset;
  
  public ParseException(String message, int offset)
  { 
    super(message+"(@"+Integer.toString(offset)+")");
    _offset=offset;
  }
  
  public ParseException(String message, int offset, Exception cause)
  {
    super(message+"(@"+Integer.toString(offset)+")",cause);
    _offset=offset;
  }

  public ParseException(int offset, Exception cause)
  {
    super(cause);
    _offset=offset;
  }
}
