package spiralcraft.lang;

public class ParseException
  extends Exception
{

  private final String _progress;
  private final int _pos;

  public ParseException(String message,int pos,String progress)
  {
    super(message);
    _pos=pos;
    _progress=progress;
  }

  public String toString()
  {
    return super.toString()+" position "+_pos+" after "+_progress;
  }
}
