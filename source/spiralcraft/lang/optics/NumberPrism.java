package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;


/**
 * An abstract Optic for representing a Number
 */
public class NumberPrism
  extends BeanPrism
{
  private static final Lense _NUMBER_DIVIDE_LENSE=new NumberDivideLense();

  public NumberPrism()
  { super(Number.class);
  }
  
  public synchronized Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException
  { 
    if (name.equals("/"))
    { 
      return new LenseBinding
        (source
        ,_NUMBER_DIVIDE_LENSE
        ,new Optic[] {focus.bind(params[0])}
        );
    }
    return super.resolve(source,focus,name,params);
  }

}

class NumberDivideLense
  implements Lense
{

  public Class getTargetClass()
  { return Number.class;
  }

  public Object translateForGet(Object source,Object[] modifiers)
  { 
    Number nsource=(Number) source;
    Number divisor=(Number) modifiers[0];
    return new Double(nsource.doubleValue()/divisor.doubleValue());
  }

  public Object translateForSet(Object source,Object[] modifiers)
  { 
    Number result=(Number) source;
    Number divisor=(Number) modifiers[0];
    return new Double(result.doubleValue()*divisor.doubleValue());
  }
}
