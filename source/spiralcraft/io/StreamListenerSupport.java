//
// Copyright (c) 2000,2012 Michael Toth
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
package spiralcraft.io;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class StreamListenerSupport
  implements StreamListener
{

  private ArrayList<WeakReference<StreamListener>> listeners
    =new ArrayList<WeakReference<StreamListener>>();

  public void add(StreamListener listener)
  { 
    if (!listeners.contains(listener))
    { listeners.add(new WeakReference<StreamListener>(listener));
    }
  }

  public void remove(StreamListener listener)
  { listeners.remove(listener);
  }

  @Override
  public void streamClosed(StreamEvent e)
  { 
    Iterator<WeakReference<StreamListener>> it=listeners.iterator();
    while (it.hasNext())
    { 
      StreamListener listener=it.next().get();
      if (listener!=null)
      { listener.streamClosed(e);
      }
      listener=null;
    }
  }
}
