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

import java.util.LinkedList;

/**
 * A Queue based on a LinkedList with synchronized operations.
 *
 * next() operations will block when the queue is empty
 *
 * If a maxLength has been specified, add() operations will block
 *   when the Queue is full.
 */
public class SynchronizedQueue
{

  private final LinkedList _list=new LinkedList();
  private int _maxLength;
  private boolean _full=false;
  private boolean _empty=true;

  public synchronized void setMaxLength(int val)
  { _maxLength=val;
  }

  public int getLength()
  { return _list.size();
  }

  public synchronized void add(Object o)
    throws InterruptedException
  { 
    while (_full)
    { wait();
    }

    _list.add(o);

    if (_empty)
    { 
      _empty=false;
      notifyAll();
    }
    else if (_list.size()==_maxLength)
    { _full=true;
    }
  }

  public synchronized void remove(Object o)
  { 
    if (_list.remove(o))
    {
      if (_full)
      { 
        _full=false;
        notifyAll();
      }
    }
    
  }

  /**
   * Obtain the next object in the queue. Blocks until
   *   an object is available.
   */
  public synchronized Object next()
    throws InterruptedException
  { 
    while (_empty)
    { wait();
    }

    try
    { return _list.removeFirst();
    }
    finally
    { 
      if (_full)
      { 
        _full=false;
        notifyAll();
      }
      else if (_list.size()==0)
      { _empty=true;
      }
    }
  }
}
