package spiralcraft.tuple;

/**
 * Metadata for data stored in Tuples. All Fields are associated with
 *   a Type. The Metadata identifies the representation, static attributes 
 *   and constraints of a given class of data. 
 *
 * Data of any Type has a representation which corresponds to single Java
 *   class.
 *
 * Compound Types are associated with a Scheme which describes the
 *   the Fields of compound data, which is normally represented by a Tuple.
 */
public interface Type
{
  /**
   * The Scheme of the compound data for this Type.
   */
  Scheme getScheme();
  
  /**
   * The public Java class or interface of data of this Type
   */
  Class getJavaClass();
}
