//
package spiralcraft.data.id;

import spiralcraft.time.Clock;

/**
 * Mints fractal ids: the epoch and timestamp of the present tick from a
 *   {@link FractalTimebase}, the minter id, then a counter.
 *
 * <p>The first id of a tick has no counter. Each later id in the tick takes
 *   the next {@link CrockfordExpandingSequence} value: 0, 1, 2, 3, 40 and
 *   so on. If the clock steps back, the sequence keeps its last tick and
 *   continues its counter, so ids never go back.
 * </p>
 *
 * <p>A prefix is an id issued for another party to extend with its own
 *   sequence. It always carries a counter value, since every later id of a
 *   tick extends the tick's first id. The sequence continues with the next
 *   value, a sibling of the prefix's, so none of its later ids extend the
 *   prefix; the expanding sequence is prefix-free, so an extension of the
 *   prefix sorts after it and before the next id, and can itself be issued
 *   as a prefix.
 * </p>
 *
 * <p>The sequence is not persisted: ids are unique across a restart only if
 *   the tick has changed, which holds unless the clock steps back across it.
 * </p>
 */
public final class FractalSequence
{
  private final FractalTimebase timebase;
  private final String minter;

  // The epoch and timestamp of the last id, and its counter
  private String lastTimePart;
  private String lastCounter;

  /**
   * @param timebase The timebase
   * @param minter The minter id, a rubric then an expanding-sequence value
   * @throws IllegalArgumentException if the minter is malformed
   */
  public FractalSequence(FractalTimebase timebase,String minter)
  {
    FractalId.checkMinter(minter);
    this.timebase=timebase;
    this.minter=minter;
  }

  public FractalTimebase getTimebase()
  { return timebase;
  }

  public String getMinter()
  { return minter;
  }

  /** Mint an id at the present time */
  public String nextId()
  { return nextIdAt(Clock.instance().timeMillis());
  }

  /** Mint ids at the present time, in order */
  public synchronized String[] nextIds(int count)
  {
    String[] ret=new String[count];
    for (int i=0;i<count;i++)
    { ret[i]=nextId();
    }
    return ret;
  }

  /** Mint an id as if the present time were the given one */
  public synchronized String nextIdAt(long timeMillis)
  {
    if (advance(timeMillis))
    { return lastTimePart+minter;
    }
    lastCounter=CrockfordExpandingSequence.next(lastCounter);
    return lastTimePart+minter+lastCounter;
  }

  /** Issue a prefix at the present time */
  public String nextPrefix()
  { return nextPrefixAt(Clock.instance().timeMillis());
  }

  /** Issue a prefix as if the present time were the given one */
  public synchronized String nextPrefixAt(long timeMillis)
  {
    advance(timeMillis);
    lastCounter=CrockfordExpandingSequence.next(lastCounter);
    return lastTimePart+minter+lastCounter;
  }

  /**
   * A block of ids: a prefix extended by the first count values of the
   *   expanding sequence, so the block sorts after the ids minted before it
   *   and before those minted after it.
   */
  public String[] nextBlock(int count)
  { return extend(nextPrefix(),count);
  }

  /** A block of ids as if the present time were the given one */
  public String[] nextBlockAt(long timeMillis,int count)
  { return extend(nextPrefixAt(timeMillis),count);
  }

  /** The first count ids under a prefix */
  public static String[] extend(String prefix,int count)
  {
    String[] ret=new String[count];
    String value=null;
    for (int i=0;i<count;i++)
    {
      value=CrockfordExpandingSequence.next(value);
      ret[i]=prefix+value;
    }
    return ret;
  }

  // Move to the tick of the given time if it is later than the last one;
  //   true if a new tick was started.
  private boolean advance(long timeMillis)
  {
    String timePart=timebase.encode(timeMillis);
    if (lastTimePart==null || timePart.compareTo(lastTimePart)>0)
    {
      lastTimePart=timePart;
      lastCounter=null;
      return true;
    }
    return false;
  }
}
