package spiralcraft.lang;

import java.util.HashMap;

import spiralcraft.lang.optics.SimpleBinding;
import spiralcraft.lang.optics.Binding;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.BeanPrism;
import spiralcraft.lang.optics.StringPrism;
import spiralcraft.lang.optics.NumberPrism;
import spiralcraft.lang.optics.BigDecimalPrism;

import java.beans.IntrospectionException;

import java.math.BigDecimal;

/**
 * Creates Optics
 */
public class OpticFactory
{
  private static final OpticFactory _INSTANCE = new OpticFactory();

  public static final OpticFactory getInstance()
  { return _INSTANCE;
  }

  private final Prism _stringPrism=new StringPrism();
  private final Prism _numberPrism=new NumberPrism();
  private final Prism _bigDecimalPrism=new BigDecimalPrism();
  private final HashMap _prismMap
    =new HashMap();

  public OpticFactory()
  {
    _prismMap.put(BigDecimal.class,_bigDecimalPrism);
    _prismMap.put(double.class,_numberPrism);
    _prismMap.put(String.class,_stringPrism);
  }


  public Optic box(Object object)
    throws BindException
  { 
    if (object instanceof Optic)
    { return (Optic) object;
    }
    else
    { return new SimpleBinding(object,true);
    }
  }

  public Focus focus(Object object)
    throws BindException
  { return new DefaultFocus(box(object));
  } 
  
  
  /**
   * Find a Prism which provides an interface into the specified Java class
   */
  public synchronized Prism findPrism(Class clazz)
    throws BindException
  { 
    Prism result=(Prism) _prismMap.get(clazz);
    if (result==null)
    {
      result=new BeanPrism(clazz);
      _prismMap.put(clazz,result);
    }
    return result;
  }
}

abstract class OpticCreator
{
  public abstract Optic createOptic(Optic source)
    throws BindException;
}

