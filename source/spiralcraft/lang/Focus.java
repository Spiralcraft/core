package spiralcraft.lang;

/**
 * Provides access to a specific set of source values a DataPipe
 *   will be evaluated against.
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
  Object getSubject();

}
