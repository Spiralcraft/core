package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;

/**
 * An abstract Optic for implementing base types
 */
public abstract class AbstractOptic
  implements Optic
{
 
  public Optic resolve(String name,Expression[] params)
  { return null;
  }

  public abstract Object get();

  public boolean set(Object value)
  { return false;
  }

  public Class getTargetClass()
  { return Object.class;
  }

}
