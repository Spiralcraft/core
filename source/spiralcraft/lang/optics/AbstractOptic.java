package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;

/**
 * An abstract Optic for implementing base types
 */
public abstract class AbstractOptic
  implements Optic
{
 
  public Optic resolve(Focus focus,String name,Expression[] params)
    throws BindException
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
