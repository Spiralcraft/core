package spiralcraft.lang;

/**
 * A Context provides a set of named Optics (data channels) for use in
 *   Expressions. A Context is normally associated with one or more Foci
 *   and provides data, tools, and or resources to use in computations. 
 
 * The particulars of the Context names and what they reference are application
 *   specific- a Context is a generic 'container' for expression evaluation.
 *
 * Within a given Expression, the Context remains static while the Focus
 *   traverses the data model as specified by the Expression syntax.
 */
public interface Context
{
  /**
   * Resolve the name by returning the Optic which corresponds to this name
   */
  public Optic resolve(String name);

  /**
   * Return the attribute names in this Context
   */
  public String[] getNames();  
}
