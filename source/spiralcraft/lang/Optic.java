package spiralcraft.lang;

import java.beans.PropertyChangeSupport;

/**
 * Provides a view of an application object or data element. Optics are combined
 *   using an Expression syntax to form Channels which navigate object models of
 *   arbitrary nature.
 *
 */
public interface Optic
{
  /**
   * Resolve the name and optional set of parameter expressions to provide a
   *   related Optic. 
   */
  Optic resolve(Focus focus,String name,Expression[] parameters)
    throws BindException;

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

  /**
   * Provide a reference to the PropertyChangeSupport object
   *   which fires a PropertyChangeEvent when the referenced data
   *   value changes. Returns null if the referenced data value does not support
   *   property change notification, or is guaranteed to never change..
   */
  PropertyChangeSupport propertyChangeSupport();

  /** 
   * Indicates whether the referenced data value is guaranteed to
   *   remain unchanged.
   */
  boolean isStatic();
}
