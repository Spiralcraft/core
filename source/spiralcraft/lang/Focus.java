package spiralcraft.lang;

/**
 * Provides a mechanism for the expression evaluation system
 *   to access a subject, an Environment, and other points of
 *   Focus relevent to expression evaluation.
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

  /**
   * Find a Focus using its well know name.
   */
  Focus findFocus(String name);

  /**
   * Return this Focus's parent Focus.
   */
  Focus getParentFocus();

  /**
   * Return a Channel, which binds the expression to this Focus.
   */
  Channel bind(Expression expression)
    throws BindException;
}
