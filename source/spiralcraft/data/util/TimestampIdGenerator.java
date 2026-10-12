//
package spiralcraft.data.util;

import spiralcraft.codec.text.CrockfordBase32Codec;
import spiralcraft.util.string.StringUtil;


//import spiralcraft.log.ClassLog;

/**
 * Generates a Crockford Base32 encoded string that is locally unique, strictly 
 *   increasing lexicographically, variable length, non-contiguous, 
 *   and embeds a timestamp at a specified resolution.
 *    
 * When multiple values are requested within the same timestamp, a lexicographically
 *   ordered sequence of increasing length is appended to the timestamp portion of
 *   the value. The sequence is a {@link CrockfordExpandingSequence}.
 *    
 * If the generator is not persisted, in oder to prevent duplicate IDs the timestamp
 *   resolution must be fine enough to ensure that the timestamp changes when the
 *   generator is re-instantiated.   
 *    
 * @author mike
 *
 */
public class TimestampIdGenerator
{
  
//  private static final ClassLog log
//    =ClassLog.getInstance(TimestampIdGenerator.class);
  
  private final long coarseness;
  private final long timeDiv;
  private final long offset;
  private final int timestampDigits;
  private volatile long lastTimestamp=0;
  private volatile String lastTimePart;
  private volatile String lastSuffix;
  
  /**
   * Create a generator.
   * 
   * @param coarseness 
   *        The power of 10 to divide the millisecond timestamp value by.
   *        A value of 0 = milliseconds. A value of 3 = seconds.
   *
   * @param timestampDigits 
   *        The number of digits to use for the timestamp. More digits can represent
   *        a longer time period.
   *
   * @param offsetTime
   *        The starting point of the timeline of sequence, if a relative timestamp
   *        will be used.
   */
  public TimestampIdGenerator(long coarseness,int timestampDigits,long offset)
  { 
    this.coarseness=coarseness;
    this.timeDiv=Double.valueOf(Math.pow(10,this.coarseness)).longValue();
    this.timestampDigits=timestampDigits;
    this.offset=offset;

  }
  
  public synchronized String[] nextId(int count)
  {
    String[] ret=new String[count];
    for (int i=0;i<count;i++)
    { ret[i]=nextId();
    }
    return ret;
  }
  
  public long timestampOf(String id)
  { 
    String tsPart=id.substring(0,timestampDigits);
    long time=CrockfordBase32Codec.decodeLong(tsPart);
    return (time*timeDiv)+offset;
  }
  
  public synchronized String nextId()
  {
    long now=(System.currentTimeMillis()-offset)/timeDiv;
//    log.fine("time: "+System.currentTimeMillis());
//    log.fine("coarseness: "+coarseness);
//    log.fine("div: "+timeDiv);
    if (lastTimePart==null && now<0)
    { 
      throw new IllegalStateException
        ("Current time is before the offset of the sequence");
    }
    
    if (now>lastTimestamp || lastTimePart==null)
    {
      lastTimestamp=now;
//      log.fine("now "+now);
      String encoded=CrockfordBase32Codec.encodeLong(now);
      if (encoded.length()>timestampDigits)
      { 
        throw new IllegalStateException
          ("Sequence timestamp cannot be represented in "+timestampDigits+" digits");
      }

      lastTimePart
        =StringUtil.prepad
          (encoded
          ,'0'
          ,timestampDigits
          );
      lastSuffix=null;
      return lastTimePart;
    }
    else
    { 
      lastSuffix=CrockfordExpandingSequence.next(lastSuffix);
      return lastTimePart+lastSuffix;
    }
    
  }
}