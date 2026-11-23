//
package spiralcraft.data.id;

import java.util.ArrayList;
import java.util.List;

/**
 * A fractal id as an object: it carries the id's string, and its parts as
 *   properties.
 *
 * <p>The layout is <code>[epoch][timestamp][minter][counter]</code>, with no
 *   delimiter:
 * </p>
 * <ul>
 *   <li>the epoch, one character, which gives the width of the timestamp
 *     from the timebase;</li>
 *   <li>the timestamp, the ticks since the start of the epoch;</li>
 *   <li>the minter, a rubric character from {@link #RUBRICS}, then a
 *     {@link CrockfordExpandingSequence} value, always present;</li>
 *   <li>the counter, absent for the first id of a tick, otherwise a chain
 *     of expanding-sequence values: the value of the sequence that minted
 *     the id, then a value for each prefix it was minted under.</li>
 * </ul>
 *
 * <p>Ids compare as their strings, which is the order they were minted in.
 * </p>
 */
public final class FractalId
  implements Comparable<FractalId>
{
  /**
   * The minter rubrics: C clients, D development (the default),
   *   P production, S staging, X personal data on a private server.
   */
  public static final String RUBRICS="CDPSX";

  private static final String[] NO_LEVELS=new String[0];

  private final String id;
  private final int epoch;
  private final String timestamp;
  private final long timestampValue;
  private final String minter;
  private final String counter;
  private final String[] counterLevels;
  private final double tickStartMillis;
  private final double tickMillis;

  /**
   * Decompose an id under a timebase.
   *
   * @throws IllegalArgumentException if the id is malformed
   */
  public static FractalId parse(String id,FractalTimebase timebase)
  { return new FractalId(id,timebase);
  }

  private FractalId(String id,FractalTimebase timebase)
  {
    if (id==null || id.isEmpty())
    { throw new IllegalArgumentException("Empty id");
    }
    this.id=id;
    this.epoch=FractalTimebase.epochOf(id.charAt(0));
    if (epoch<0)
    { throw new IllegalArgumentException("Bad epoch in id "+id);
    }

    int width=timebase.getWidth(epoch);
    int pos=1+width;
    if (id.length()<pos)
    { throw new IllegalArgumentException
        ("Id "+id+" is shorter than its timestamp");
    }
    this.timestamp=id.substring(1,pos);
    long value=0;
    for (int i=0;i<width;i++)
    {
      int d=FractalTimebase.DIGITS.indexOf(timestamp.charAt(i));
      if (d<0)
      { throw new IllegalArgumentException("Bad timestamp in id "+id);
      }
      value=(value<<5)|d;
    }
    this.timestampValue=value;
    this.tickMillis=timebase.getTickMillis(epoch);
    this.tickStartMillis=timebase.getTickStartMillis(epoch,value);

    int minterEnd=minterEnd(id,pos);
    this.minter=id.substring(pos,minterEnd);

    this.counter=id.substring(minterEnd);
    if (counter.isEmpty())
    { this.counterLevels=NO_LEVELS;
    }
    else
    {
      List<String> levels=new ArrayList<>();
      int start=0;
      while (start<counter.length())
      {
        int end=start+lengthAt(counter,start,id);
        if (end>counter.length())
        { throw new IllegalArgumentException("Truncated counter in id "+id);
        }
        levels.add(counter.substring(start,end));
        start=end;
      }
      this.counterLevels=levels.toArray(NO_LEVELS);
    }
  }

  /**
   * Check a minter id: a rubric, then an expanding-sequence value.
   *
   * @throws IllegalArgumentException if the minter is malformed
   */
  public static void checkMinter(String minter)
  {
    if (minter==null || minterEnd(minter,0)!=minter.length())
    { throw new IllegalArgumentException("Not a minter id: "+minter);
    }
  }

  // The end of the minter that starts at pos
  private static int minterEnd(String s,int pos)
  {
    if (pos+1>=s.length() || RUBRICS.indexOf(s.charAt(pos))<0)
    { throw new IllegalArgumentException("No minter in "+s+" at "+pos);
    }
    int end=pos+1+lengthAt(s,pos+1,s);
    if (end>s.length())
    { throw new IllegalArgumentException("Truncated minter in "+s);
    }
    for (int i=pos+1;i<end;i++)
    {
      if (FractalTimebase.DIGITS.indexOf(s.charAt(i))<0)
      { throw new IllegalArgumentException("Bad minter in "+s);
      }
    }
    return end;
  }

  private static int lengthAt(String s,int pos,String id)
  {
    try
    { return CrockfordExpandingSequence.lengthAt(s,pos);
    }
    catch (IllegalArgumentException x)
    { throw new IllegalArgumentException("Bad character in id "+id);
    }
  }

  /** The id's string */
  public String getId()
  { return id;
  }

  /** The epoch, 0 to 31 */
  public int getEpoch()
  { return epoch;
  }

  /** The epoch's character */
  public char getEpochCharacter()
  { return id.charAt(0);
  }

  /** The timestamp's characters */
  public String getTimestamp()
  { return timestamp;
  }

  /** The timestamp's value, ticks since the start of the epoch */
  public long getTimestampValue()
  { return timestampValue;
  }

  /** The minter id: rubric and sequence */
  public String getMinter()
  { return minter;
  }

  /** The minter's rubric character */
  public char getMinterRubric()
  { return minter.charAt(0);
  }

  /** The minter's sequence value, after the rubric */
  public String getMinterSequence()
  { return minter.substring(1);
  }

  /** The counter, empty for the first id of a tick */
  public String getCounter()
  { return counter;
  }

  /**
   * The counter's levels: the minting sequence's value, then one for each
   *   prefix the id was minted under
   */
  public String[] getCounterLevels()
  { return counterLevels.clone();
  }

  /** When the id's tick begins, in milliseconds since the Unix epoch */
  public double getTickStartMillis()
  { return tickStartMillis;
  }

  /** The length of the id's tick, in milliseconds */
  public double getTickMillis()
  { return tickMillis;
  }

  /** The parts of the id, described */
  public String describe()
  {
    return id
      +": epoch "+getEpochCharacter()
      +", timestamp "+timestamp
      +" (tick of "+String.format("%.3f",tickMillis)+" ms at "
      +FractalTimebaseParts.time(tickStartMillis)+")"
      +", minter "+minter
      +", counter "+(counter.isEmpty()?"none":String.join(" ",counterLevels))
      ;
  }

  @Override
  public int compareTo(FractalId other)
  { return id.compareTo(other.id);
  }

  @Override
  public boolean equals(Object o)
  { return o instanceof FractalId && ((FractalId) o).id.equals(id);
  }

  @Override
  public int hashCode()
  { return id.hashCode();
  }

  /** The id's string */
  @Override
  public String toString()
  { return id;
  }
}
