package spiralcraft.util.string;

public class IntegerToDecimalString
  extends NumberToDecimalString<Integer>
{
  
  public IntegerToDecimalString(String format)
  { super(format);
  }

  @Override
  public Integer coerce(Number val)
  { return val!=null?val.intValue():null;
  }
  
  
}