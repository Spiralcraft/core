package spiralcraft.tuple;

/**
 * Provides access to a collection of data via the interfaces in the Tuple
 *   package.
 */
public interface Store
{
  /**
   *@return The Tuple with the specified Scheme and TupleId, or null
   *  if the Tuple does not exist.
   */
  public Tuple find(Scheme scheme,TupleId id);
  
  /**
   * Create a new Tuple for the specified Scheme.
   *
   *@return A new Tuple in buffered state.
   */
  public Tuple create(Scheme scheme);
}
