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
 *   the value.
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
  private volatile long suffix;
  private volatile int suffixDigits;
  
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
    this.timeDiv=Double.valueOf(Math.pow(10,coarseness)).longValue();
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
    if (now>lastTimestamp)
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
      suffix=0;
      suffixDigits=0;
      return lastTimePart;
    }
    else
    { 
      switch (suffixDigits)
      {
        case 0:
          suffixDigits++;
          return lastTimePart+"0";
        default:
          suffix++;
          if (suffix>= (Math.pow(32L,suffixDigits)/8)*suffixDigits)
          { 
            suffix*=32;
            suffixDigits++;
          }
          return lastTimePart+CrockfordBase32Codec.encodeLong(suffix);
      }
      
    }
    
  }
}
