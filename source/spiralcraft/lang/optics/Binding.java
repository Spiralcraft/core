package spiralcraft.lang.optics;

import java.beans.PropertyChangeSupport;

import spiralcraft.lang.Optic;

/**
 * Standard internal basis for implementation of the Optic interface, which
 *   requires a local cache to eliminate redundant object creation.
 *
 * To summarize, a Binding provides an updateable "view" of a piece of 
 *   information from an underlying data source or data container.
 */
public interface Binding
  extends Optic
{
  /**
   * Return the bound object
   */
  public Object get();

  /**
   * Update the bound object.
   *@return Whether the modification was successful or not.
   */
  public boolean set(Object value);

  /**
   * Indicate the Prism associated with the bound Object. The Prism
   *   is the means by which further Bindings are resolved against
   *   this Binding.
   */
  public Prism getPrism();

  /** 
   * Indicates whether the bound object is guaranteed to
   *   remain unchanged.
   */
  boolean isStatic();

  /**
   * Provide a reference to the PropertyChangeSupport object
   *   which fires a PropertyChangeEvent when the bound object changes.
   *   Returns null if the binding does not support
   *   property change notification, or is guaranteed to never change..
   */
  PropertyChangeSupport propertyChangeSupport();

  /**
   * Return the cache which hold bindings which derive their value from
   *   this binding.
   */
  WeakBindingCache getCache();
  
}
