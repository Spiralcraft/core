//
package spiralcraft.data.id;

import java.time.Instant;

/**
 * An explanation of a {@link FractalTimebase}: its base and origin, and for
 *   each epoch its width, window, tick, and the times it begins and ends.
 */
public final class FractalTimebaseParts
{
  /** One epoch of a timebase */
  public static final class Epoch
  {
    public final char character;
    public final int index;
    public final int width;
    /** The window, the end of the epoch, in seconds since the origin */
    public final double windowSeconds;
    public final double tickMillis;
    /** When the epoch begins, in milliseconds since the Unix epoch */
    public final double startMillis;
    /** When it ends, the first time in the next epoch */
    public final double endMillis;

    Epoch(FractalTimebase timebase,int index)
    {
      this.index=index;
      this.character=FractalTimebase.DIGITS.charAt(index);
      this.width=timebase.getWidth(index);
      this.tickMillis=timebase.getTickMillis(index);
      this.startMillis=timebase.getEpochStartMillis(index);
      this.endMillis=timebase.getEpochEndMillis(index);
      this.windowSeconds
        =(endMillis-timebase.getOriginMillis())/1000;
    }

    public char getCharacter()
    { return character;
    }

    public int getIndex()
    { return index;
    }

    public int getWidth()
    { return width;
    }

    public double getWindowSeconds()
    { return windowSeconds;
    }

    public double getTickMillis()
    { return tickMillis;
    }

    public double getStartMillis()
    { return startMillis;
    }

    public double getEndMillis()
    { return endMillis;
    }

    @Override
    public String toString()
    {
      return character
        +" width "+width
        +", tick "+String.format("%.3f",tickMillis)+" ms"
        +", "+time(startMillis)+" to "+time(endMillis)
        ;
    }
  }

  public final String spec;
  public final int base;
  public final long originMillis;
  public final Epoch[] epochs;

  FractalTimebaseParts(FractalTimebase timebase)
  {
    this.spec=timebase.getSpec();
    this.base=timebase.getBase();
    this.originMillis=timebase.getOriginMillis();
    this.epochs=new Epoch[FractalTimebase.EPOCHS];
    for (int i=0;i<epochs.length;i++)
    { epochs[i]=new Epoch(timebase,i);
    }
  }

  public String getSpec()
  { return spec;
  }

  public int getBase()
  { return base;
  }

  public long getOriginMillis()
  { return originMillis;
  }

  public Epoch[] getEpochs()
  { return epochs;
  }

  // An instant, or the number of years for times beyond Instant's range
  static String time(double millis)
  {
    if (Math.abs(millis/1000)<Instant.MAX.getEpochSecond())
    { return Instant.ofEpochMilli((long) millis).toString();
    }
    return String.format("%.3g years",millis/(365.25*86_400_000));
  }

  @Override
  public String toString()
  {
    StringBuilder b=new StringBuilder();
    b.append("Timebase ").append(spec)
      .append(": base ").append(base)
      .append(", origin ").append(time(originMillis));
    for (Epoch epoch:epochs)
    { b.append("\n  ").append(epoch);
    }
    return b.toString();
  }
}
