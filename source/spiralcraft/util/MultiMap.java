package spiralcraft.util;

import java.util.Map;
import java.util.List;
/**
 * An extension of the Map interface which maps Lists of values
 *   with a single key.
 */
public interface MultiMap<K,V>
  extends Map<K,List<V>>
{

  /**
   * Associate a key with a collection that contains a single value
   */
  public void set(K key,V value);

  /**
   * Append the value to the collection associated with the specified key.
   */
  public void add(K key,V value);
  
  /**
   * Remove the value from the collection associated with the specified key.
   */
  public void remove(K key,V value);
  
  /**
   * Return a single value from the collection associated with the specified key
   */
  public V getOne(K key);
  
  
}
