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
package spiralcraft.util.listener;

import java.util.IdentityHashMap;
import java.util.LinkedList;

/**
 * <p>Manages multiple listeners as a single unit
 * </p>
 * 
 * @author mike
 *
 * @param <I>
 */
public class ListenerSupport<Tevent>
  implements Listener<Tevent>
{

  private IdentityHashMap<Listener<Tevent>,Boolean> map
    =new IdentityHashMap<Listener<Tevent>,Boolean>();
  private LinkedList<Listener<Tevent>> list
    =new LinkedList<Listener<Tevent>>();
  
  @Override
  public void handleEvent(Tevent event)
  {
    for (Listener<Tevent> listener : list)
    { listener.handleEvent(event);
    }
  }
  
  public synchronized void add(Listener<Tevent> listener)
  {
    if (!map.containsKey(listener))
    { 
      map.put(listener,Boolean.TRUE);
      list.add(listener);
    }
  }
  
  public synchronized void remove(Listener<Tevent> listener)
  {
    if (map.remove(listener)==Boolean.TRUE)
    { list.remove(listener);
    }
  }
  
}
