package spiralcraft.util.string;

/**
 * Converts names between Spiralcraft identifiers (the in side) and the
 *   names of an external form (the out side), such as the members of a
 *   JSON document, independent of any particular use.
 *
 * A converter is named after its two sides, in then out
 *   (CamelSnakeConverter: camelCase in, snake_case out). Each direction
 *   undoes the other for the names in the converter's contract.
 */
public interface IdentifierConverter
{
  /** External name -> Spiralcraft identifier */
  String toIn(String name);

  /** Spiralcraft identifier -> external name */
  String toOut(String name);
}
