package spiralcraft.tuple;

import java.util.List;

/**
 * A class of of Tuple.
 */
public interface Scheme
{
  /**
   * Return an immutable Collection of the Fields contained in Tuples associated with
   *   this Scheme. The list will be ordered by the field index.
   */ 
  FieldList getFields();
}
