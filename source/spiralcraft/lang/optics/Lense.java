package spiralcraft.lang.optics;

/**
 * A bidirectional data translation associated with an Optic.
 */
public interface Lense
{
  /**
   * Transform the source object in the 'get' direction
   */
  public Object translateForGet(Object source,Object[] modifiers);

  /**
   * Transform the source object in the 'set' direction
   */
  public Object translateForSet(Object source,Object[] modifiers);

  /**
   * Return the Prism associated with the target Object.
   */
  public Prism getPrism();
}
