package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import java.math.BigDecimal;

/**
 * An abstract Optic for representing a Number
 */
public class NumberOptic
  extends BeanOptic
{
  public NumberOptic(Optic source)
    throws BindException
  { super(source);
  }
  
  public Optic resolve(Focus focus,String name,Expression[] params)
    throws BindException
  {
    if (name.equals("/"))
    { return new NumberDivideOptic(this,params[0].createChannel(focus));
    }
    return super.resolve(focus,name,params);
  }
}

class NumberDivideOptic
  extends NumberOptic
{
  private final Optic _divisor;

  public NumberDivideOptic(Optic source,Optic divisor)
    throws BindException
  { 
    super(source);
    _divisor=divisor;
  }

  public Object get()
  { 
    Number source=(Number) super.get();
    Number divisor=(Number) _divisor.get();
    return new Double(source.doubleValue()/divisor.doubleValue());
  }

  

}
