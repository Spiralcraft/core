package spiralcraft.lang;


/**
 * A part of a Channel which provides a view of an application object or data element, potentially derived from one or
 *   more views from related Optics.
 */
public interface Optic
{
  /**
   * Resolve the name and optional set of parameters to provide another Optic 
   *   based on this one. 
   */
  Optic derive(String name,Expression[] parameters);

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

}
