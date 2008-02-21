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
package spiralcraft.lang.spi;

import spiralcraft.lang.Channel;

import java.util.HashMap;

public class WeakChannelCache
{
  // XXX Make weak?
  private HashMap<Object,Channel<?>> _map
    =new HashMap<Object,Channel<?>>();
  
  @SuppressWarnings("unchecked") // Map has heterogeneous types
  public synchronized <X> Channel<X> get(Object key)
  { return (Channel<X>) _map.get(key);
  }
  
  public synchronized void put(Object key,Channel<?> value)
  { _map.put(key,value);
  }
}

