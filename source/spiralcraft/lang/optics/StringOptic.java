package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;

/**
 * An abstract Optic for representing a String
 */
public abstract class StringOptic
  extends AbstractOptic
{
  
  public Class getTargetClass()
  { return String.class;
  }
}
