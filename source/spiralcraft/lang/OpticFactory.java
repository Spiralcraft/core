package spiralcraft.lang;

import java.util.HashMap;

import spiralcraft.lang.optics.BeanOptic;
import spiralcraft.lang.optics.NumberOptic;
import spiralcraft.lang.optics.BigDecimalOptic;

import java.beans.IntrospectionException;

import java.math.BigDecimal;

/**
 * Creates Optics
 */
public class OpticFactory
{
  private static final HashMap _decoratorMap=new HashMap();

  private static final OpticCreator _bigDecimalCreator
    =new OpticCreator()
      {
        public Optic createOptic(Optic source)
          throws BindException
        { return new BigDecimalOptic(source);
        }
      };
    
  private static final OpticCreator _numberCreator
    =new OpticCreator()
      {
        public Optic createOptic(Optic source)
          throws BindException
        { return new NumberOptic(source);
        }
      };

  static 
  { 
    _decoratorMap.put(BigDecimal.class,_bigDecimalCreator);
    _decoratorMap.put(double.class,_numberCreator);
  }

  /**
   * Create an Optic which decorates the source Optic
   *   based on the source Optic's targetClass.
   */
  public static Optic decorate(Optic source)
  { 
    OpticCreator creator
      =(OpticCreator) _decoratorMap.get(source.getTargetClass());

    try
    {
      if (creator==null)
      { return new BeanOptic(source);
      }
      else
      { return creator.createOptic(source);
      }
    }
    catch (BindException x)
    { return source;
    }
  }


}

abstract class OpticCreator
{
  public abstract Optic createOptic(Optic source)
    throws BindException;
}

