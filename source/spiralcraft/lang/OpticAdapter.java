package spiralcraft.lang;

import java.beans.PropertyChangeSupport;


/**
 * Default implementation of an Optic.
 */
public class OpticAdapter
  implements Optic
{
  /**
   * Return null. no names exposed
   */
  public Optic resolve(Focus focus,String name,Expression[] parameters)
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
   * The underlying value is not guaranteed to remain unchanged
   */
  public boolean isStatic()
  { return false;
  }

  public Class getTargetClass()
  { return Object.class;
  }

  /**
   * Property change not supported by default
   */
  public PropertyChangeSupport propertyChangeSupport()
  { return null;
  }

}
