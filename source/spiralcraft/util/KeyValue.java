//
// Copyright (c) 2012 Michael Toth
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

import spiralcraft.common.Immutable;

/**
 * An immutable association between a key and a value
 * 
 * @author mike
 *
 * @param <K>
 * @param <V>
 */
@Immutable
public class KeyValue<K,V>
{

  protected final K key;
  protected final V value;
  
  public KeyValue(K key,V value)
  { 
    this.key=key;
    this.value=value;
  }
  
  public K getKey()
  { return key;
  }
  
  public V getValue()
  { return value;
  }
  
  @Override
  public String toString()
  { return super.toString()+"[ ["+key+"] = ["+value+"]";
  }
}
