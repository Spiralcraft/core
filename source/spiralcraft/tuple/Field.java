package spiralcraft.tuple;

/**
 * A description of a single element of a Tuple.
 */
public interface Field
{
  /**
   * The Scheme to which this Field belongs
   */
  Scheme getScheme();
  
  /**
   * The index of the Field within the Scheme, which corresponds to the
   *   ordinal position of the associated value within the Tuple
   */
  int getIndex();
  
  /**
   * The name of the Field, to be used for the programmatic binding of
   *   data consumers and producers to Tuples of this Scheme.
   */
  String getName();
  
  /**
   * The Type of the Field
   */
  Type getType();
  
}
