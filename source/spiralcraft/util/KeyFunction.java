package spiralcraft.util;

/**
 * Key functions are used by data structures to generate stable hashable and
 *   sortable keys for organizing data.
 */
public interface KeyFunction<K,V>
{
  /**
   * Return a key derived from the given value. The same value must always
   *   return the same or equivalent key.
   */
  public K key(V value);
}
