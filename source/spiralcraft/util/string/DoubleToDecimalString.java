package spiralcraft.util.string;

public class DoubleToDecimalString
  extends NumberToDecimalString<Double>
{
  public DoubleToDecimalString(String format)
  { super(format);
  }  

  @Override
  public Double coerce(Number val)
  { return val!=null?val.doubleValue():null;
  }
  
  
}