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
 * Associates a Key with a Value
 */
public class Association<K,V>
{
  private K key;
  private V value;

  public Association()
  {
  }

  public void setKey(K key)
  { this.key=key;
  }

  public void setValue(V value)
  { this.value=value;
  }

  public K getKey()
  { return key;
  }

  public V getValue()
  { return value;
  }
}
