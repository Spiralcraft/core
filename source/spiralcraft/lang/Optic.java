package spiralcraft.lang;

import java.beans.PropertyChangeSupport;

// XXX use Type
import spiralcraft.lang.optics.Prism;

/**
 * An Optic is a "data pipe" that provides a "view" of the contents of an 
 *   arbitrary data source or container, the "model-part", that exists within
 *   an arbitrary object model.
 *
 * An Optic is able to track changes to the model-part over time, and in
 *   some cases may update the contents of the model-part.
 *
 * As a "view", the Optic is transformative- it provides a specific
 *   representation of the model-part contents which may consist of a subset
 *   or superset of the information contained within the model-part.
 *
 * Optics are combined using an expression language to form Channels. A Channel
 *   is a tree of Optics that results in the combination and transformation
 *   of a number of model-parts into a single representation.
 *
 * Optics are "typed"- they are associated with a content type
 *   (expressed as a Java class) and a set of named resolutions
 *   that provide "deeper" views into the object model, alternate
 *   representations of the model-part, or transformations of the model-part
 *   content controlled by other contextual elements
 *
 * Resolutions are handled by the resolve() method. A given resolution is
 *   specified by name and may be associated with a Focus and a set of
 *   Expression parameters. As such, transformations have access to the
 *   application context via the Focus and define the context
 *   in which the parameter expressions will be evaluated.
 *
 * Optics may be 'decorated' with interfaces that provide support for 
 *   certain operations, such as Iterator. The decorate() method will
 *   resolve an appropriate implementation of the desired interface and
 *   bind it to the Optic, if such an implementation exists and is 
 *   appropriately registered with the OpticFactory.
 */
public interface Optic
{
  /**
   * Resolve the name and optional set of parameter expressions to provide
   *   new views derived from this one.
   */
  Optic resolve(Focus focus,String name,Expression[] parameters)
    throws BindException;

  /**
   * Return the content of this view.
   */
  Object get();

  /**
   * Update the content this view, if the transformation associated with
   *   this Optic and its data sources is reversible.
   *
   *@return Whether the modification was successful or not.
   */
  boolean set(Object value);

  /**
   * Indicate the Java Class of the content of this View.
   */
  Class getContentType();

  /**
   * Decorate this Optic with a suitable implementation of a decoratorInterface
   *   for the content type.
   *return The decorator that implements the decoratorInterface, or null if
   *  the decoratorInterface is not supported
   */
  Decorator decorate(Class decoratorInterface);
 
  /**
   * Provide a reference to the PropertyChangeSupport object
   *   which fires a PropertyChangeEvent when the data source for this view
   *   changes. Returns null if the this view or any of its data sources do
   *   not support property change notification, or if the content is guaranteed
   *   to remain unchanged.
   */
  PropertyChangeSupport propertyChangeSupport();

  /** 
   * Indicates whether the referenced data value is guaranteed to
   *   remain unchanged.
   */
  boolean isStatic();
  
  /**
   * Return the spiralcraft.lang type of the referenced data 
   */
  Prism getPrism();
}
