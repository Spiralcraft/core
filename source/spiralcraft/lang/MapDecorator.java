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
package spiralcraft.lang;

import java.util.Iterator;


/**
 * <p>Provides access to the Map aspect of a target object
 * </p>
 * 
 * <p>The MapDecorator is bound to its source Channel
 * </p>
 *
 */
public abstract class MapDecorator<T,K,V>
  implements Decorator<T>
{   
  
  protected final Channel<T> source;
  private final Reflector<K> keyReflector;
  private final Reflector<V> valueReflector;
  
  public MapDecorator
    (Channel<T> source
    ,Reflector<K> keyReflector
    ,Reflector<V> valueReflector
    )
  { 
    if (keyReflector==null)
    { 
      throw new IllegalArgumentException
        ("Key reflector cannot be null. Source="+source);
    }
    if (valueReflector==null)
    { 
      throw new IllegalArgumentException
        ("Value reflector cannot be null. Source="+source);
    }
    this.source=source;
    this.keyReflector=keyReflector;
    this.valueReflector=valueReflector;
  }
  
  /**
   * 
   * @return The Reflector which describes the key type of the Map.
   *   
   */
  public Reflector<K> getKeyReflector()
  { return keyReflector;
  }
  
  /**
   * 
   * @return The Reflector which describes the value type of the Map.
   *   
   */
  public Reflector<V> getValueReflector()
  { return valueReflector;
  }
   
  public boolean put(K key,V value)
  { return put(source.get(),key,value);
  }
  
  public V get(K key)
  { return get(source.get(),key);
  }
  
  public Iterator<K> keys()
  { return keys(source.get());
  }
  
  public Iterator<V> values()
  { return values(source.get());
  }
  
  public Channel<T> getSource()
  { return source;
  }
  
  public abstract boolean put(T map,K key,V value);

  public abstract V get(T map,K key);
  
  public abstract Iterator<K> keys(T map);
  
  public abstract Iterator<V> values(T map);
  

}
