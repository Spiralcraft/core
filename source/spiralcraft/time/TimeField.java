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

import java.util.Calendar;
import java.util.HashMap;

/**
 * <p>An unit of a time expression. TimeField objects are
 *   recognized by various Chronologies by identity.
 * </p>
 * 
 * <p>Common TimeFields are represented by static members of this class.
 * </p>
 * 
 * @author mike
 *
 */
public class TimeField
{
  private static final HashMap<String,TimeField> map
    =new HashMap<>();
  
  public static final TimeField fromString(String label)
  { return map.get(label);
  }
  
  public static final String[] values()
  { return map.keySet().toArray(new String[map.size()]);
  }
  
  public static final TimeField MILLISECOND
    =new TimeField
    ("millisecond"
    ,Chronom.MILLISECOND
    ,Chronom.SECOND
    ,1000
    ,Calendar.MILLISECOND
    ,0
    ,null
    );

  public static final TimeField SECOND_OF_MINUTE
    =new TimeField
    ("second"
    ,Chronom.SECOND
    ,Chronom.MINUTE
    ,60
    ,Calendar.SECOND
    ,0
    ,MILLISECOND
    );
  
  public static final TimeField MINUTE_OF_HOUR
    =new TimeField
    ("minute"
    ,Chronom.MINUTE
    ,Chronom.HOUR
    ,60
    ,Calendar.MINUTE
    ,0
    ,SECOND_OF_MINUTE
    );

  public static final TimeField HOUR_OF_DAY
    =new TimeField
    ("hour"
    ,Chronom.HOUR
    ,Chronom.DAY
    ,24
    ,Calendar.HOUR_OF_DAY
    ,0
    ,MINUTE_OF_HOUR
    );
  
  public static final TimeField HOUR_OF_HALFDAY
    =new TimeField
    ("ampmHour"
    ,Chronom.HOUR
    ,Chronom.HALFDAY
    ,12
    ,Calendar.HOUR
    ,0
    ,MINUTE_OF_HOUR
    );


  public static final TimeField HALFDAY_OF_DAY
    =new TimeField
    ("ampm"
    ,Chronom.HALFDAY
    ,Chronom.DAY
    ,2
    ,Calendar.AM_PM
    ,0
    ,HOUR_OF_DAY
    );
  
    
  public static final TimeField DAY_OF_WEEK
    =new TimeField
    ("weekday"
    ,Chronom.DAY
    ,Chronom.WEEK
    ,7
    ,Calendar.DAY_OF_WEEK
    ,1
    ,HOUR_OF_DAY
    );

  public static final TimeField DAY_OF_MONTH
    =new TimeField
    ("day"
    ,Chronom.DAY
    ,Chronom.MONTH
    ,0
    ,Calendar.DAY_OF_MONTH
    ,1
    ,HOUR_OF_DAY
    );
  
  public static final TimeField WEEK_OF_MONTH
    =new TimeField
    ("monthweek"
    ,Chronom.WEEK
    ,Chronom.MONTH
    ,0
    ,Calendar.WEEK_OF_MONTH
    ,0
    ,DAY_OF_WEEK
    );

  public static final TimeField WEEK_OF_YEAR
    =new TimeField
    ("week"
    ,Chronom.WEEK
    ,Chronom.YEAR
    ,0
    ,Calendar.WEEK_OF_YEAR
    ,0
    ,DAY_OF_WEEK
    );
  
  public static final TimeField MONTH_OF_YEAR
    =new TimeField
    ("month"
    ,Chronom.MONTH
    ,Chronom.YEAR
    ,0
    ,Calendar.MONTH
    ,0
    ,DAY_OF_MONTH
    );

  public static final TimeField YEAR
    =new TimeField
    ("year"
    ,Chronom.YEAR
    ,null
    ,0
    ,Calendar.YEAR
    ,0
    ,MONTH_OF_YEAR
    );
     
  protected final String label;
  protected final Chronom unit;
  protected final Chronom inUnit;
  protected final long modulus;
  protected final int calendarField;
  protected final int calendarMinValue;
  protected final TimeField subPeriod;
  
  private TimeField
    (String label
    ,Chronom unit
    ,Chronom inUnit
    ,long modulus
    ,int calendarField
    ,int calendarMinValue
    ,TimeField subPeriod
    )
  {
    this.label=label;
    this.unit=unit;
    this.inUnit=inUnit;
    this.modulus=modulus;
    this.calendarField=calendarField;
    this.calendarMinValue=calendarMinValue;
    this.subPeriod=subPeriod;
    map.put(label,this);
  }
  
  @Override
  public String toString()
  { return label;
  }
  
  public String toString(int value)
  { return label+"="+value;
  }
  
  public void set(Calendar calendar,int value)
  { calendar.set(calendarField,value);
  }
  
  public int get(Calendar calendar)
  { return calendar.get(calendarField);
  }
  
  /**
   * Clear this specific field from the calendar
   * 
   * @param calendar
   */
  public void clear(Calendar calendar)
  { calendar.set(calendarField,calendarMinValue);
  }
  
  /**
   * Clear this field and all subperiods from the calendar, effectively
   *   setting the calendar to zero point with respect to this TimeField
   * 
   * @param calendar
   */
  public void truncate(Calendar calendar)
  { 
    clear(calendar);
    if (subPeriod!=null)
    { subPeriod.truncate(calendar);
    }
  }
  
  /**
   * Truncate the subperiod, effectively setting the calendar to the start
   *   of the calendar period represented by this TimeField
   * 
   * @param calendar
   */
  public void truncateRest(Calendar calendar)
  {
    if (subPeriod!=null)
    { subPeriod.truncate(calendar);
    }
  }
}
