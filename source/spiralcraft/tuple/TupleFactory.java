package spiralcraft.tuple;


/**
 * Creates Tuple implementations
 */
public interface TupleFactory
{
  /**
   * Create a Tuple with the specified scheme.
   *
   * The Tuple will be in a buffered state and will be
   *   ready to accept data.
   *   
   */
  public Tuple createTuple(Scheme scheme);
}
