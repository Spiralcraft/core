package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import java.math.BigDecimal;

/**
 * An abstract Optic for representing a Number
 */
public class BigDecimalPrism
  extends BeanPrism
{
  private static final Lense _BIG_DECIMAL_DIVIDE_LENSE=new BigDecimalDivideLense();

  public BigDecimalPrism()
  { super(BigDecimal.class);
  }
  
  public Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException
  {
    if (name.equals("/"))
    {   
      return new LenseBinding
        (source
        ,_BIG_DECIMAL_DIVIDE_LENSE
        ,new Optic[] {focus.bind(params[0])}
        );
    }
    return super.resolve(source,focus,name,params);
  }
}

class BigDecimalDivideLense
  implements Lense
{
  public Class getTargetClass()
  { return BigDecimal.class;
  }

  public Object translateForGet(Object source,Object[] modifiers)
  { 
    BigDecimal nsource=(BigDecimal) source;
    BigDecimal divisor=(BigDecimal) modifiers[0];
    return nsource.divide(divisor,BigDecimal.ROUND_UNNECESSARY);
  }

  public Object translateForSet(Object source,Object[] modifiers)
  { 
    BigDecimal result=(BigDecimal) source;
    BigDecimal divisor=(BigDecimal) modifiers[0];
    return result.multiply(divisor);
  }

}
