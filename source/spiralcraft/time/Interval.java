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

/**
 * <p>A specific period of time represented by a start Instant and an end
 *   Instant. An Interval is normalized so the start Instant is always less 
 *   than or equal to the end Instant.
 * </p>
 * 
 * <p>An Instant is defined as being contained in an Interval if it is
 *   greater or equal to the start instant and less than the end instant.
 * </p>

 * <p>An Interval is immutable
 * </p>
 * 
 * 
 * @author mike
 *
 */
public class Interval
{
  private final Instant start;
  private final Instant end;
  private final int hashCode;
  private Duration duration;
  
  
  public Interval(Instant start,Instant end)
  { 
    if (start.compareTo(end)<=0)
    {
      this.start=start;
      this.end=end;
    }
    else
    { 
      this.start=end;
      this.end=start;
    }

    this.hashCode=37*start.hashCode()+37*end.hashCode();
  }
  
  public Instant getStart()
  { return start;
  }
  
  public Instant getEnd()
  { return end;
  }
  
  public Duration duration()
  { 
    if (duration==null)
    { duration=end.duration(start);
    }
    return duration;
  }
  
  @Override
  public int hashCode()
  { return hashCode;
  }
  
  @Override
  public boolean equals(Object o)
  { 
    if (o==null)
    { return false;
    }
    
    if (o instanceof Interval)
    { 
      if (o.hashCode()!=hashCode)
      { return false;
      }
      
      Interval interval=(Interval) o;
      return interval.start.equals(start) && interval.end.equals(end);
      
    }
    return false;
  }
  
  /**
   * Compute whether the Interval contains an Instant. An instant is
   *   contained if it is equal to or greater than the start Instant,
   *   and strictly less than the end Instant.
   * 
   * @param instant
   * @return
   */
  public boolean contains(Instant instant)
  { return start.compareTo(instant)<=0 && end.compareTo(instant)>0;
  }

  /**
   * Compute whether the Interval strictly contains another Interval, defined
   *   as this Interval contains both endpoints of the contained interval.
   *   
   * 
   * @param instant
   * @return
   */
  public boolean contains(Interval interval)
  { return contains(interval.start) && contains(interval.end);
  }
}
