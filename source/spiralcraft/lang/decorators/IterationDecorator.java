package spiralcraft.lang.decorators;

import spiralcraft.lang.Decorator;
import spiralcraft.lang.Optic;

/**
 * Supports Iteration through containers and data structures with multiple
 *   elements.
 *
 * The Optic interface exposes the data at the current position of the 
 *   iteration.
 */
public interface IterationDecorator
  extends Decorator,Optic
{
  /**
   * Reset the iteration
   */
  public void reset();
  
  /**
   * Indicate whether there are more elements in the iteration
   */
  public boolean hasNext();
  
  /**
   * Advance to the next element
   */
  public void next();
}
