package spiralcraft.util;

import java.util.Map;

/**
 * An extension of the Map interface to support mechanisms which associate
 *   multiple values with a single key.
 */
public interface MultiMap
  extends Map
{

  /**
   * Associate a key with a collection that contains a single value
   */
  public void set(Object key,Object value);

  /**
   * Append the value to the collection associated with the specified key.
   */
  public void add(Object key,Object value);
  
  /**
   * Remove the value from the collection associated with the specified key.
   */
  public void remove(Object key,Object value);
  
  /**
   * Return a single value from the collection associated with the specified key
   */
  public Object getOne(Object key);
  
  
}
