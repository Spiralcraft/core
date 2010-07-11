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

import java.util.Calendar;

/**
 * <p>A unit of Time measurement
 * </p>
 * 
 * <p>Objects of this class are immutable singletons. By convention,
 *   implementations have a static UNIT field which contains the
 *   singleton instance.
 * </p>
 * 
 * @author mike
 */
public enum Chronom
{ 
  NANOSECOND(BigDecimal.valueOf(0.000000001),-1)
  ,MILLISECOND(BigDecimal.valueOf(0.001),Calendar.MILLISECOND)
  ,SECOND(BigDecimal.ONE,Calendar.SECOND)
  ,MINUTE(new Duration(60,SECOND),Calendar.MINUTE)
  ,HOUR(new Duration(60,MINUTE),Calendar.HOUR_OF_DAY)
  ,HALFDAY(new Duration(12,HOUR),-1)
  ,DAY(new Duration(24,HOUR),Calendar.DAY_OF_YEAR)
  ,WEEK(new Duration(7,DAY),Calendar.WEEK_OF_YEAR)
  ,MONTH(BigDecimal.valueOf(30.417).multiply(DAY.standardSeconds),Calendar.MONTH)
  ,QUARTER(new Duration(3,MONTH),-1)
  ,YEAR(BigDecimal.valueOf(365.25).multiply(DAY.standardSeconds),Calendar.YEAR)
  ,DECADE(new Duration(10,YEAR),-1)
  ,CENTURY(new Duration(100,YEAR),-1)
  ,MILLENIUM(new Duration(1000,YEAR),-1)
  ;
  
  
  private final BigDecimal standardSeconds;
  private final Duration duration;
  private final int calendarField;
  
  
  private Chronom(BigDecimal standardSeconds,int calendarField)
  { 
    this.standardSeconds=standardSeconds;
    this.duration=null;
    this.calendarField=calendarField;
  }
  
  private Chronom(Duration duration,int calendarField)
  {
    this.standardSeconds=duration.getStandardSeconds();
    this.duration=duration;
    this.calendarField=calendarField;
  }
  
  
  public BigDecimal getStandardSeconds()
  { return standardSeconds;
  }
  
  public long getStandardMilliseconds()
  { return standardSeconds.multiply(BigDecimal.valueOf(1000)).longValueExact();
  }
  
  public Duration getDuration()
  { return duration;
  }
  
  public int getCalendarField()
  { return calendarField;
  }
  
  /**
   * <p>A Duration composed of the specified count of this unit
   * </p>
   * 
   * @param count
   * @return
   */
  public Duration times(long count)
  { return new Duration(count,this);
  }
  
  public Duration add(long count,Duration fine)
  { return new Duration(count,this,fine);
  }
  
  /**
   * Return a Duration in terms of a Chronom that is associated with a 
   *   field of a calendar implementation. 
   * 
   * @return
   */
  public Duration getCalendarDuration()
  { 
    Chronom test=this;
    long multiple=1;
    while (test.calendarField==-1)
    { 
      multiple=multiple*test.duration.getCount();
      test=test.duration.getUnit();
    }
    return new Duration(multiple,test);
    
  }
  
}
