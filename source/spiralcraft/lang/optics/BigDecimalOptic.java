package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import java.math.BigDecimal;

/**
 * An abstract Optic for representing a Number
 */
public class BigDecimalOptic
  extends BeanOptic
{
  public BigDecimalOptic(Optic source)
    throws BindException
  { super(source);
  }
  
  public Optic resolve(Focus focus,String name,Expression[] params)
    throws BindException
  {
    if (name.equals("/"))
    { return new BigDecimalDivideOptic(this,params[0].createChannel(focus));
    }
    return super.resolve(focus,name,params);
  }
}

class BigDecimalDivideOptic
  extends BigDecimalOptic
{
  private final Optic _divisor;

  public BigDecimalDivideOptic(Optic source,Optic divisor)
    throws BindException
  { 
    super(source);
    _divisor=divisor;
  }

  public Object get()
  { 
    BigDecimal source=(BigDecimal) super.get();
    BigDecimal divisor=(BigDecimal) _divisor.get();
    return source.divide(divisor,BigDecimal.ROUND_UNNECESSARY);
  }

}
