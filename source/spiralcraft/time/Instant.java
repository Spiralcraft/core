//
// Copyright (c) 2010 Michael Toth
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

import java.math.BigDecimal;

import spiralcraft.common.Immutable;


/**
 * <p>A specific point in time relative to the Posix EPOCH of 
 *   January 1, 1970 00:00:00 UTC
 * </p>
 * 
 * <p>An Instant is immutable
 * </p>
 * 
 * <p>An instant is interoperable with the java.util.Date and 
 *   java.util.Calendar object
 * </p>
 * 
 * @author mike
 *
 */
@Immutable
public class Instant
  implements Comparable<Instant>
{
  protected final long offset;
  protected final Chronom resolution;
  protected final int hashCode;
  
  /**
   * Construct an instant that represents the current time in the 
   *   specific resolution at the best available accuracy
   */
  public Instant(Chronom resolution)
  { 

    
    
    switch (resolution)
    {
      case NANOSECOND:
        
        this.offset=Clock.instance().timeNanos();
        break;
      case MILLISECOND:
      
        this.offset=Clock.instance().timeMillis();
        break;
      
      case SECOND:
        this.offset=Clock.instance().timeMillis()/1000;
        break;

      default:
        BigDecimal seconds=Chronom.MILLISECOND.getStandardSeconds()
          .multiply(BigDecimal.valueOf(Clock.instance().timeMillis()));
        long offset=seconds.divide(resolution.getStandardSeconds())
          .longValueExact();
        this.offset=offset;
    }
    
    this.resolution=resolution;
    this.hashCode=getOffsetSeconds().hashCode();
    
  }
  
  /**
   * Construct an instant that represents the approximate current time in 
   *   milliseconds since the POSIX epoch according the the default Clock
   *   precision (usually 100ms).
   */
  public Instant()
  { this(Clock.instance().approxTimeMillis(),Chronom.MILLISECOND);
  }
  
  /**
   * Construct an instant that represents a number of milliseconds relative
   *   to the POSIX epoch according the System clock.
   */
  public Instant(long offsetMillis)
  { this(offsetMillis,Chronom.MILLISECOND);
  }
  
  /**
   * Construct an instant that represents an offset to the POSIX epoch of
   *   of the specified resolution.
   */
  public Instant(long offset,Chronom resolution)
  {
    this.resolution=resolution;
    this.offset=offset;
    this.hashCode=getOffsetSeconds().hashCode();
  }
  

  /**
   * Return an interval that starts with this Instant and ends at the specified
   *  instant. An Interval with a positive Duration will be computed if the
   *  other Instant is in the future.
   *   
   * @param other
   * @return
   */
  public Interval interval(Instant other)
  { return new Interval(this,other);
  }
    
  /**
   * Subtract another Instant from this one, resulting in the Duration
   *   between the two instants. If the other instant is in the past,
   *   a positive Duration will be computed.
   * 
   * @param other
   * @return
   */
  public Duration duration(Instant other)
  {
    if (this.resolution==other.resolution)
    { return new Duration(this.offset-other.offset,resolution);
    }
    else
    { 
      return new Duration
        (this.getOffsetMillis()-other.getOffsetMillis()
        ,Chronom.MILLISECOND
        );
    }
    
  }
    
  /**
   * The offset of this instant with respect to the epoch of the associated
   *   Calendar.
   *   
   * @return
   */
  public long getOffset()
  { return offset;
  }
  
  /**
   * <p>The resolution of the offset value (eg. Second, Millisecond, etc.)
   * </p>
   * 
   * <p>If an Instant of a coarser precision is used in a computation with
   *   an Instant of a finer precision, this Instant will be converted
   *   to the earliest instant of the finer precision, eg.. the first 
   *   Millisecond of a Second, first Second of a Day, etc.
   * </p>
   * 
   * @return
   */
  public Chronom getResolution()
  { return resolution;
  }
    
  
  /**
   * <p>Get the fractional number of seconds since the Unix epoch
   * </p>
   */
  public BigDecimal getOffsetSeconds()
  { return resolution.getStandardSeconds().multiply(BigDecimal.valueOf(offset));
  }


  /**
   * <p>Get the number of milliseconds since the Unix epoch
   * </p>
   * 
   * @return
   */
  public long getOffsetMillis()
  { 
    switch (resolution)
    {
      case MILLISECOND:
        return offset;
      case NANOSECOND:
        return offset/1000000;
      case SECOND:
        return offset*1000;
      default:
        return resolution.getStandardSeconds()
          .multiply(BigDecimal.valueOf(1000))
          .longValueExact();
    }
  }
  
  @Override
  public boolean equals(Object o)
  {
    if (o==null || !(o instanceof Instant))
    { return false;
    }
    Instant oi=(Instant) o;
    if (oi.getResolution()==resolution)
    { return offset==oi.getOffset();
    }
    else
    { return getOffsetSeconds().equals(oi.getOffsetSeconds());
    }
  }
  
  @Override
  public int hashCode()
  { return hashCode;
  }
  
  @Override
  public int compareTo(Instant o)
  {
    if (o.getResolution()==resolution)
    { return Long.valueOf(offset).compareTo(Long.valueOf(o.getOffset()));
    }
    else
    { return getOffsetSeconds().compareTo(o.getOffsetSeconds());
    }
    
  }
  
  @Override
  public String toString()
  { return ""+offset+" "+resolution.name();
  }
}
