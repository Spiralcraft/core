package spiralcraft.lang;

/**
 * Provides a means for a channel to access the environment and 
 *   the subject in focus.
 */
public interface Focus
{
    
  /**
   * Return the Environment which resolves
   *   names for this Focus.
   */
  Environment getEnvironment();

  /**
   * Return the subject of expression evaluation
   */
  Optic getSubject();

}
