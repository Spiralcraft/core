package spiralcraft.time;

/**
 * Updates time from the System clock at a specified
 *   precision (defaults to 250ms).
 */
public final class Clock
  implements Runnable
{

  private static final Clock _INSTANCE=new Clock();

  private int _precision=250;
  private final Thread _thread=new Thread(this,"Clock");
  private long _time=System.currentTimeMillis();
  private Object _lock=new Object();

  Clock()
  {
    _thread.setDaemon(true);
    _thread.start();
  }

  public static final Clock instance()
  { return _INSTANCE;
  }

  /**
   * Specify the precision of the clock.
   */
  public final void setPrecision(int millis)
  { _precision=millis;
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

  public final void run()
  {
    try
    {
      while (true)
      {
        synchronized (_lock)
        { _time=System.currentTimeMillis();
        }
        Thread.currentThread().sleep(_precision);
      }
    }
    catch (InterruptedException x)
    { x.printStackTrace();
    }

  }
}
