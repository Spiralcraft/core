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
  private final Lense _divideLense;

  public BigDecimalPrism()
  { 
    super(BigDecimal.class);
    _divideLense=new BigDecimalDivideLense(this);
  }
  
  public Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException
  {
    if (name.equals("/"))
    {   
      return new LenseBinding
        (source
        ,_divideLense
        ,new Optic[] {focus.bind(params[0])}
        );
    }
    return super.resolve(source,focus,name,params);
  }
}

class BigDecimalDivideLense
  implements Lense
{
  private final Prism _prism;
  
  public BigDecimalDivideLense(Prism prism)
  { _prism=prism;
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
  
  public Prism getPrism()
  { return _prism;
  }

}
