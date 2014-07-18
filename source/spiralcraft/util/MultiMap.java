//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
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
  public void removeValue(K key,V value);
  
  /**
   *@return the first value from the collection associated with the specified 
   *  key, or null if there are no values associated with the key
   */
  public V getFirst(K key);
  
  /**
   *@return the last value from the collection associated with the specified 
   *   key, or null if there are no values associated with the key
   */
  public V getLast(K key);
  
}
