package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;

/**
 * An abstract Optic for representing a Number
 */
public abstract class NumberOptic
  extends AbstractOptic
{
  
  public Class getTargetClass()
  { return Number.class;
  }
}
