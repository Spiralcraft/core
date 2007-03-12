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

/**
 * Key functions are used by data structures to generate stable hashable and
 *   sortable keys for organizing data.
 */
public interface KeyFunction<K,V>
{
  /**
   * Return a key derived from the given value. The same value must always
   *   return the same or equivalent key. The returned key must be immutable,
   *   ie. its externally accessible data must not change once it is returned.
   */
  public K key(V value);
}
