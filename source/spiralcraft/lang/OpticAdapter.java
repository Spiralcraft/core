package spiralcraft.lang;

import java.beans.PropertyChangeSupport;

import spiralcraft.lang.optics.Prism;

/**
 * Default implementation of an Optic.
 */
public abstract class OpticAdapter
  implements Optic
{
  /**
   * Return null. no names exposed
   */
  public Optic resolve(Focus focus,String name,Expression[] parameters)
    throws BindException
  { return null;
  }

  /**
   * The target is null
   */
  public Object get()
  { return null;
  };

  /**
   * The target cannot be modified 
   */
  public boolean set(Object value)
  { return false;
  }

  /**
   * No immediate decorator support
   */
  public Decorator decorate(Class decoratorInterface)
  { return null;
  }
  
  /**
   * The underlying value is not guaranteed to remain unchanged
   */
  public boolean isStatic()
  { return false;
  }

  public final Class getContentType()
  { return getPrism().getContentType();
  }

  /**
   * Property change not supported by default
   */
  public PropertyChangeSupport propertyChangeSupport()
  { return null;
  }

  /**
   * This needs to be implemented by the subclass
   */
  public abstract Prism getPrism();
}
