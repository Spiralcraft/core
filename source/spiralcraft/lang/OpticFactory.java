package spiralcraft.lang;

import java.util.HashMap;

import spiralcraft.lang.optics.BeanOptic;

import java.beans.IntrospectionException;

/**
 * Creates Optics
 */
public class OpticFactory
{
  private static final HashMap _decoratorMap=new HashMap();

  /**
   * Create an Optic which decorates the source Optic
   *   based on the source Optic's targetClass.
   */
  public static Optic decorate(Optic source)
  { 
    OpticCreator creator
      =(OpticCreator) _decoratorMap.get(source.getTargetClass());

    if (creator==null)
    { 
      try
      { return new BeanOptic(source);
      }
      catch (IntrospectionException x)
      { return source;
      }
    }
    else
    { return creator.createOptic(source);
    }
  }

  abstract class OpticCreator
  {
    public abstract Optic createOptic(Optic source);
  }

}
