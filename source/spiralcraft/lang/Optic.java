package spiralcraft.lang;


/**
 * A part of a Channel which provides a view of an application object or data element, potentially derived from one or
 *   more views provided by related Optics.
 */
public interface Optic
{
  /**
   * Resolve the name and optional set of parameters to provide another Optic 
   *   based on this one. 
   */
  Optic resolve(String name,Expression[] parameters);

  /**
   * Return the application object or data value referenced by this DataPipe as evaluated against
   *   the specified DataContext
   */
  Object get();

  /**
   * Update the application object or data value referenced by this DataPipe within the specified DataContext.
   *@return Whether the modification was successful or not.
   */
  boolean set(Object value);

  /**
   * Indicate the Java Class of the target object.
   */
  Class getTargetClass();

}
