package spiralcraft.lang;

/**
 * Environments provide a context for the resolution of names in expressions.
 *
 * Expressions are resolved against a Focus. A Focus provides a 'subject', which
 *   represents an arbitrary Object that will be 'examined' by the Expression.
 *
 * A Focus also provides an Environment, through which expressions can access
 *   other information relevent to evaluating expressions against the subject. 
 *   An Environment is a namespace against which certain names in expressions are
 *   resolved. The actual names exposed by this namespace are determined by the
 *   application developer. For example, the expression "client.title" will look up
 *   the name "client" in the Environment and resolve the name "title" against
 *   the returned Optic.
 *
 * The Environment serves as a container for Attributes, which map names to Optics.
 */
public interface Environment
{
  /**
   * Resolve the name by returning the Optic which corresponds to this name
   */
  public Optic resolve(String name);

  /**
   * Return the attribute names in this Environment
   */
  public String[] getNames();  
}
