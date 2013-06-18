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

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class WeakChannelCache
{
  private HashMap<Object,WeakReference<Channel<?>>> _map
    =new HashMap<Object,WeakReference<Channel<?>>>();
  
  @SuppressWarnings("unchecked") // Map has heterogeneous types
  public synchronized <X> Channel<X> get(Object key)
  { 
    WeakReference<Channel<?>> ref=_map.get(key);
    if (ref!=null)
    {
      @SuppressWarnings("rawtypes")
      Channel chan=ref.get();
      if (chan==null)
      { _map.remove(key);
      }
      else
      { return chan;
      }
    }
    return null;
  }
  
  public synchronized void put(Object key,Channel<?> value)
  { _map.put(key,new WeakReference<Channel<?>>(value));
  }
}

