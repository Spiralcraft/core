package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

/**
 * Defines the type of view provided by an Optic.
 *
 * Defines the Java class of the content provided by the view.
 * 
 * Defines a set of named transformations that can be derived from an
 *   associated Optic to create a new Optic that provides a view of some aspect
 *   of the original Optic. 
 *
 */
public interface Prism
{

  /**
   * Generate a new Binding which resolves the name and the given parameter 
   *   expressions against the source Binding within the context of the supplied
   *   Focus.
   */
  public Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException;

  /**
   * Decorate the specified optic with a decorator that implements the
   *   specified interface
   */
  public Decorator decorate(Binding source,Class decoratorInterface)
    throws BindException;
  
  /**
   * Return the Java class of the data object accessible through Bindings 
   *   associated with this Prism
   */
  public Class getContentType();
}
