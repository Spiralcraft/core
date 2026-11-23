//
package spiralcraft.data.id;

/**
 * The time scale of fractal ids: how the time since an origin becomes the
 *   epoch and timestamp characters at the head of an id, and back.
 *
 * <p>A timebase is written as a compact spec, for example
 *   <code>A76A79869XCFV2</code>:
 * </p>
 * <ul>
 *   <li>the base, one character (<code>A</code> = 10), which places the
 *     epochs on the time scale;</li>
 *   <li>(count, width) pairs, one character each, until the counts cover
 *     the 32 epochs, a count of <code>0</code> meaning 32: here 7 epochs
 *     with a timestamp of width 6, 10 at width 7, 9 at 8 and 6 at 9;</li>
 *   <li>optionally the origin: the code <code>X</code>, days since the Unix
 *     epoch, then the day as a {@link CrockfordExpandingSequence} value
 *     (<code>CFV2</code> is day 20,454, 2026-01-01). Without an origin the
 *     origin is the Unix epoch.</li>
 * </ul>
 *
 * <p>The time scale: the window of an epoch is
 *   <code>Wmin &#215; f^(32 &#215; base + epoch)</code> seconds of time since
 *   the origin, with <code>Wmin</code> the Planck time and <code>f</code>
 *   chosen to spread 1024 windows evenly up to about 10^110 years. Epoch
 *   <code>e</code> covers the elapsed time from the end of the window before
 *   it (from 0 for epoch 0) to the end of its own, and uses its full range:
 *   its tick is that span over <code>32^width</code>, and its timestamp
 *   counts ticks from the epoch's own start. The epoch of a time is the
 *   first one whose window still holds it, so it is derived, never stored,
 *   and rises with time.
 * </p>
 *
 * <p>Every calculation of the scheme is here, so that every implementation
 *   decodes the same ids. The windows are computed with StrictMath, which
 *   gives the same result on every JVM.
 * </p>
 */
public final class FractalTimebase
{
  /** The Planck time in seconds, the bottom of the time scale */
  public static final double MIN_WINDOW_SECONDS=5.391247e-44;

  /** The ratio of one window to the one before it */
  public static final double WINDOW_FACTOR=1.435995;

  /** The number of epochs, one for each digit */
  public static final int EPOCHS=32;

  /** The origin code for days since the Unix epoch */
  public static final char ORIGIN_UNIX_DAYS='X';

  static final String DIGITS="0123456789ABCDEFGHJKMNPQRSTVWXYZ";

  private static final long MILLIS_PER_DAY=86_400_000L;

  private final String spec;
  private final int base;
  private final long originMillis;
  private final int[] widths=new int[EPOCHS];

  // Elapsed milliseconds since the origin at the end of each epoch
  private final double[] endMillis=new double[EPOCHS];

  // The length of a tick in each epoch, in milliseconds
  private final double[] tickMillis=new double[EPOCHS];

  /**
   * Parse a timebase spec.
   *
   * @throws IllegalArgumentException if the spec is malformed
   */
  public static FractalTimebase parse(String spec)
  { return new FractalTimebase(spec);
  }

  private FractalTimebase(String spec)
  {
    if (spec==null || spec.isEmpty())
    { throw new IllegalArgumentException("Empty timebase spec");
    }
    this.spec=spec;
    this.base=digit(spec,0);

    // (count, width) pairs until the counts cover every epoch
    int pos=1;
    int epoch=0;
    while (epoch<EPOCHS)
    {
      if (pos+1>=spec.length())
      { throw new IllegalArgumentException
          ("Timebase spec "+spec+" covers only "+epoch+" epochs");
      }
      int count=digit(spec,pos);
      if (count==0)
      { count=32;
      }
      int width=digit(spec,pos+1);
      if (width==0 || width>12)
      { throw new IllegalArgumentException
          ("Timebase spec "+spec+" has a width outside 1 to 12");
      }
      if (epoch+count>EPOCHS)
      { throw new IllegalArgumentException
          ("Timebase spec "+spec+" covers more than "+EPOCHS+" epochs");
      }
      for (int i=0;i<count;i++)
      { widths[epoch++]=width;
      }
      pos+=2;
    }

    // The origin
    if (pos==spec.length())
    { originMillis=0;
    }
    else if (spec.charAt(pos)==ORIGIN_UNIX_DAYS)
    {
      String day=spec.substring(pos+1);
      if (day.isEmpty()
          || CrockfordExpandingSequence.lengthAt(day,0)!=day.length()
          )
      { throw new IllegalArgumentException
          ("Timebase spec "+spec+" has a malformed origin day");
      }
      originMillis
        =Math.multiplyExact
          (CrockfordExpandingSequence.positionOf(day),MILLIS_PER_DAY);
    }
    else
    { throw new IllegalArgumentException
        ("Timebase spec "+spec+" has an unknown origin code "
        +spec.charAt(pos)
        );
    }

    // The time scale
    double startMillis=0;
    for (int e=0;e<EPOCHS;e++)
    {
      endMillis[e]
        =MIN_WINDOW_SECONDS
          *StrictMath.pow(WINDOW_FACTOR,32*base+e)
          *1000
          ;
      tickMillis[e]
        =(endMillis[e]-startMillis)/StrictMath.pow(32,widths[e]);
      startMillis=endMillis[e];
    }
  }

  private static int digit(String spec,int pos)
  {
    int d=DIGITS.indexOf(spec.charAt(pos));
    if (d<0)
    { throw new IllegalArgumentException
        ("Timebase spec "+spec+" has a bad character at "+pos);
    }
    return d;
  }

  /** The spec this timebase was parsed from */
  public String getSpec()
  { return spec;
  }

  /** The base, the position of epoch 0 on the time scale over 32 */
  public int getBase()
  { return base;
  }

  /** The origin, in milliseconds since the Unix epoch */
  public long getOriginMillis()
  { return originMillis;
  }

  /** The width of the timestamp in an epoch */
  public int getWidth(int epoch)
  { return widths[epoch];
  }

  /** The length of a tick in an epoch, in milliseconds */
  public double getTickMillis(int epoch)
  { return tickMillis[epoch];
  }

  /** When an epoch begins, in milliseconds since the Unix epoch */
  public double getEpochStartMillis(int epoch)
  { return originMillis+(epoch==0?0:endMillis[epoch-1]);
  }

  /**
   * When an epoch ends, in milliseconds since the Unix epoch: the first
   *   time in the next epoch.
   */
  public double getEpochEndMillis(int epoch)
  { return originMillis+endMillis[epoch];
  }

  /**
   * The epoch that holds a time.
   *
   * @throws IllegalArgumentException if the time is before the origin
   * @throws IllegalStateException if the time is beyond the last epoch
   */
  public int epochAt(long timeMillis)
  {
    double elapsed=timeMillis-originMillis;
    if (elapsed<0)
    { throw new IllegalArgumentException
        ("Time "+timeMillis+" is before the origin of timebase "+spec);
    }
    for (int e=0;e<EPOCHS;e++)
    {
      if (elapsed<endMillis[e])
      { return e;
      }
    }
    throw new IllegalStateException
      ("Time "+timeMillis+" is beyond the last epoch of timebase "+spec);
  }

  /** The timestamp value of a time within its epoch */
  public long timestampAt(int epoch,long timeMillis)
  {
    double sinceStart=timeMillis-getEpochStartMillis(epoch);
    long value=(long) Math.floor(sinceStart/tickMillis[epoch]);
    // Guard the edges against rounding
    long max=(1L<<(5*widths[epoch]))-1;
    return Math.max(0,Math.min(max,value));
  }

  /**
   * The head of an id minted at a time: the epoch character, then the
   *   timestamp, at the width of the epoch.
   */
  public String encode(long timeMillis)
  {
    int epoch=epochAt(timeMillis);
    int width=widths[epoch];
    long value=timestampAt(epoch,timeMillis);
    char[] c=new char[width+1];
    c[0]=DIGITS.charAt(epoch);
    for (int i=width;i>0;i--)
    {
      c[i]=DIGITS.charAt((int) (value&31));
      value>>>=5;
    }
    return new String(c);
  }

  /**
   * When the tick of a timestamp value begins, in milliseconds since the
   *   Unix epoch
   */
  public double getTickStartMillis(int epoch,long value)
  { return getEpochStartMillis(epoch)+value*tickMillis[epoch];
  }

  /** The digit value of an epoch character, or -1 if it is not one */
  public static int epochOf(char c)
  { return DIGITS.indexOf(c);
  }

  /** Explain the timebase, epoch by epoch */
  public FractalTimebaseParts explain()
  { return new FractalTimebaseParts(this);
  }

  @Override
  public String toString()
  { return spec;
  }
}
