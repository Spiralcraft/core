package spiralcraft.lang;

/**
 * A locus for the evaluation of Expressions
 *
 * A Focus references a single subject and is associated with a 
 *   Context. The Context provides access to the 'workspace' of a given
 *   task, and as such will usually contain the subject of the Focus as well
 *   as a number of other named attributes which provide access to data, tools
 *   and resources useful to a given computation.  
 *
 * Expressions bound to a Focus incorporate attributes of the subject
 *   and the Context into traversals, transformations and computations to
 *   create new subjects of Focus. 
 */
public interface Focus
{
    
  /**
   * Return the Context of this Focus (the 'workspace' in which the computation
   *   is being performed).
   */
  Context getContext();

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
   * Return a Channel, which binds the Expression to this Focus. It may be
   *   useful for implementations to cache Channels to avoid creating
   *   multiple channels for the same Expression.
   */
  Channel bind(Expression expression)
    throws BindException;
}
