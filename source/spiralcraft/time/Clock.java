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
package spiralcraft.time;

import spiralcraft.util.ArrayUtil;

/**
 * Updates time from the System clock at a specified
 *   precision. The static instance is set to a precision
 *   of 100ms.
 */
public final class Clock
  implements Runnable
{

  private static final Clock _INSTANCE=new Clock(100);

  private final int _precision;
  private final Thread _thread=new Thread(this,"Clock");
  private long _time=System.currentTimeMillis();
  private long _nanoOffset=(System.currentTimeMillis() * 1000000)-(System.nanoTime());
  private Object _lock=new Object();

  private ClockListener[] _clockListeners;

  Clock(int precisionMillis)
  { 
    _precision=precisionMillis;
    _thread.setDaemon(true);
    _thread.start();
  }

  public static final Clock instance()
  { return _INSTANCE;
  }

  /**
   * Obtain the approximate time in milliseconds.
   */
  public final long approxTimeMillis()
  { 
    synchronized (_lock)
    { return _time;
    }
  }

  /**
   * Obtain the current time according to System.currentTimeMillis()
   * 
   * @return
   */
  public final long timeMillis()
  { return System.currentTimeMillis();
  }
  
  /**
   * Obtain the current time according to System.nanoTime() offset to
   *   be relative to System.currentTimeMillis() * 1e+6
   *   
   * @return
   */
  public final long timeNanos()
  { return System.nanoTime()+_nanoOffset;
  }
  
  
  public synchronized void addClockListener(ClockListener listener)
  { 
    if (_clockListeners!=null)
    { _clockListeners=new ClockListener[] {listener};
    }
    else
    { _clockListeners=ArrayUtil.append(_clockListeners,listener);
    }
  }

  public synchronized void removeClockListener(ClockListener listener)
  { _clockListeners=ArrayUtil.remove(_clockListeners,listener);
  }

  @Override
  public final void run()
  {
    try
    {
      while (true)
      {
        update();
        Thread.sleep(_precision);
      }
    }
    catch (InterruptedException x)
    { x.printStackTrace();
    }

  }

  private final void update()
  {
    long time;
    synchronized (_lock)
    { 
      _time=System.currentTimeMillis();
      time=_time;
    }
    if (_clockListeners!=null)
    { 
      for (int i=0;i<_clockListeners.length;i++)
      { _clockListeners[i].timeChanged(time);
      }
    }
  }
}
