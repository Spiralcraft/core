package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;

/**
 * Defines a data type used for Expression evaluation.
 * 
 * Represents the 'interface' exposed by a Binding by mapping names and
 *   parameter sets to other Bindings which expose properties and methods of
 *   the data object accessible through this binding.
 * 
 * Prisms are used to support Optic.resolve() for a specific data 'type' (java
 *   class or other type mechanism) independently of the actual source of data.
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
   * Return the Java class of the data object accessible through Bindings 
   *   associated with this Prism
   */
  public Class getJavaClass();
}
