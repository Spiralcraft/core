package spiralcraft.tuple;

/**
 * A description of a single element of a Tuple.
 */
public interface Field
{
  /**
   * Return the Scheme to which this Field belongs
   */
  Scheme getScheme();
  
  /**
   * Return the index of the Field within the Scheme.
   */
  int getIndex();
}
