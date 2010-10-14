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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import spiralcraft.log.ClassLog;

public enum Chronology
{
  
  GREGORIAN
    (TimeField.YEAR
    ,TimeField.MONTH_OF_YEAR
    ,TimeField.DAY_OF_MONTH
    ,TimeField.HOUR_OF_DAY
    ,TimeField.MINUTE_OF_HOUR
    ,TimeField.SECOND_OF_MINUTE
    ,TimeField.MILLISECOND
    )
  {

    @Override
    public Calendar newCalendarImpl(
      TimeZone timeZone,
      Locale locale)
    { return new GregorianCalendar(timeZone,locale);
    }

    

  };
  

  private static final ClassLog log
    =ClassLog.getInstance(Chronology.class);
  
  private final TimeField[] timeFields;
  private final Chronom[] units;
  
  private Chronology(TimeField ... timeFields)
  { 
    this.timeFields=timeFields;
    units=new Chronom[timeFields.length];
    int i=0;
    for (TimeField field: timeFields)
    { units[i++]=field.unit;
    }
  }
  
  protected abstract Calendar newCalendarImpl(TimeZone timeZone,Locale locale);
  
  public Iterator<Instant> iterate
    (TimeZone timeZone
    ,Locale locale
    ,Instant start
    ,TimeField field
    ,int interval
    ,Instant stop
    )
  {
    Calendar calendar=newCalendarImpl(timeZone,locale);
    calendar.setTimeInMillis(start.getOffsetMillis());
    return new FieldIterator(calendar,field,interval,stop);
  };
  
  public Instant add
    (TimeZone timeZone
    ,Locale locale
    ,Instant start
    ,TimeField field
    ,int amount
    )
  { 
    Calendar calendar=newCalendarImpl(timeZone,locale);
    calendar.setTimeInMillis(start.getOffsetMillis());
    calendar.add(field.calendarField,amount);
    return new Instant(calendar.getTimeInMillis());
  }
  
  public TimeX add
    (TimeZone timeZone
    ,Locale locale
    ,TimeX start
    ,TimeField field
    ,int amount
    )
  { 
    Calendar calendar=newCalendarImpl(timeZone,locale);
    setCalendarFields(calendar,start);
    calendar.add(field.calendarField,amount);
    return toTimeX(calendar,timeFields);
  }

  public Instant add
    (TimeZone timeZone
    ,Locale locale
    ,Instant start
    ,Duration duration
    )
  { 
    Calendar calendar=newCalendarImpl(timeZone,locale);
    calendar.setTimeInMillis(start.getOffsetMillis());
    Duration rest=duration;
    while (rest!=null)
    { 

      Duration calDuration=rest.getUnit().getCalendarDuration();
      int calAmount
        =Long.valueOf(rest.getCount()*calDuration.getCount()).intValue();
      calendar.add(calDuration.getUnit().getCalendarField(),calAmount);
      rest=rest.getRest();
    }
    return new Instant(calendar.getTimeInMillis());
    
  }
  
  public Instant subtract
    (TimeZone timeZone
    ,Locale locale
    ,Instant end
    ,Duration duration
    )
  { 
    Calendar calendar=newCalendarImpl(timeZone,locale);
    calendar.setTimeInMillis(end.getOffsetMillis());
    Duration rest=duration;
    while (rest!=null)
    { 

      Duration calDuration=rest.getUnit().getCalendarDuration();
      int calAmount
        =Long.valueOf(rest.getCount()*calDuration.getCount()).intValue();
      calendar.add(calDuration.getUnit().getCalendarField(),-calAmount);
      rest=rest.getRest();
    }
    return new Instant(calendar.getTimeInMillis());

  }
  
  /**
   * Compute a Duration from an time range in terms of a specific set of units
   *   as they relate to the Chronology
   */
  public Duration subtract
    (TimeZone timeZone
    ,Locale locale
    ,Instant end
    ,Instant start
    )
  { return subtract(timeZone,locale,end,start,units);
  }
  
  /**
   * Compute a Duration from an time range in terms of a specific set of units
   *   as they relate to the Chronology. If the duration is smaller than the
   *   finest specified unit, the method returns null.
   * 
   * @param timeZone
   * @param locale
   * @param end
   * @param start
   * @param units
   * @return
   */
  public Duration subtract
    (TimeZone timeZone
    ,Locale locale
    ,Instant end
    ,Instant start
    ,Chronom[] units
    )
  {
    if (units.length==0)
    { throw new IllegalArgumentException("Units must be specified");
    }
    boolean reverse=start.getOffsetMillis()>end.getOffsetMillis();
    if (reverse)
    { 
      Instant swap=end;
      end=start;
      start=swap;
    }
    
    Calendar startCalendar=newCalendarImpl(timeZone,locale);
    startCalendar.setTimeInMillis(start.getOffsetMillis());
    
    Duration duration=null;
    
    int unitNum=0;
    
    long startMs=start.getOffsetMillis();
    long endMs=end.getOffsetMillis();
    
    while (startMs < endMs && unitNum<units.length)
    {

      Chronom unit=units[unitNum];
      long unitSum=0;
      Duration calDuration=unit.getCalendarDuration();
      long calDurationMs=calDuration.getStandardMilliseconds();
      
      while (endMs-startMs>calDurationMs)
      {
        // Iterate until we clear the undershoot
        
        long unitCount=(endMs-startMs)/calDuration.getStandardMilliseconds();
        int calUnits=Long.valueOf(unitCount*calDuration.getCount()).intValue();
        
        startCalendar.add
          (calDuration.getUnit().getCalendarField()
          ,calUnits
          );
        unitSum+=unitCount;
        startMs=startCalendar.getTimeInMillis();
      }
      
      while (startMs>endMs)
      { 
        log.fine("Overshot by "+(startMs-endMs));
        int calUnits=Long.valueOf(calDuration.getCount()).intValue();

        // Back off until we clear the overshoot
        startCalendar.add
          (calDuration.getUnit().getCalendarField()
          ,-calUnits
          );
        unitSum--;
        startMs=startCalendar.getTimeInMillis();
        
      }
     
      if (unitSum!=0)
      { 
        if (duration==null)
        { duration=new Duration((reverse?-1:1)*unitSum,unit);
        }
        else
        { duration=duration.add((reverse?-1:1)*unitSum,unit);
        }
      }
      unitNum++;
    }
    
//    if (duration==null)
//    { log.fine("start="+start+", end="+end+", units[0]="+units[0]+", units[n]="+units[units.length-1]);
//    }
    return duration;
  }
  

  
  /**
   * <p>Rebase a TimeX from an original time to within a single unit of 
   *   a destination time. Used to move time expressions between absolute
   *   intervals while preserving relative fields. 
   * </p>
   * 
   * <p>Specifically, this sets all
   *   fields more significant that the specified field to the destination time
   *   while advancing the specified field a given amount, leaving fields
   *   less significant than the specified field unchanged.
   * </p>
   * 
   * 
   * 
   * @param timeZone
   * @param locale
   * @param original
   * @param dest
   * @param field
   * @param offset
   * @return
   */
  public TimeX rebase
    (TimeZone timeZone
    ,Locale locale
    ,TimeX original
    ,Instant destInstant
    ,TimeField field
    ,int offset
    )
  {
    Calendar calendar=newCalendarImpl(timeZone,locale);
    setCalendarFields(calendar,original);
    
    Calendar destCalendar=newCalendarImpl(timeZone,locale);
    destCalendar.setTimeInMillis(destInstant.getOffsetMillis());
      
    TimeField lastField=null;
    for (TimeField destField:timeFields)
    { 
      if (field.unit.compareTo(destField.unit)>0)
      { 
        // We've passed the rebase point
        break;
      }
      lastField=destField;
      if (destCalendar.isSet(destField.calendarField))
      {
        int value=destCalendar.get(destField.calendarField);
        log.fine("Rebasing "+destField.label+" to "+value);
        calendar.set(destField.calendarField,value);
      }
    }
    
    if (lastField==null || field.unit!=lastField.unit)
    { 
      // The specified field is a precision somewhere between the
      //   standard calendar fields- eg. WEEK_OF_YEAR is between
      //   MONTH_OF_YEAR and DAY_OF_MONTH
      if (destCalendar.isSet(field.calendarField))
      {
        int value=destCalendar.get(field.calendarField);
        log.fine("Rebasing tweener "+field.label+" to "+value);
        calendar.set(field.calendarField,value);
      }
      else
      {
        log.fine(" tweener "+field.label+" is not set");
        
      }
    }
    calendar.add(field.calendarField,offset);
    return toTimeX(calendar,timeFields);
  }
  
  private void setCalendarFields(Calendar calendar,TimeX time)
  { 
    for (TimeX.TimeValue timeValue: time.getValues())
    { calendar.set(timeValue.field.calendarField,timeValue.value);
    }
  }

  /**
   * Convert an Instant into a time expression using this Chronology
   * 
   * @param timeZone
   * @param locale
   * @param instant
   * @return
   */
  public TimeX toTimeX(TimeZone timeZone,Locale locale,Instant instant)
  {

    Calendar calendar=newCalendarImpl(timeZone,locale);
    calendar.setTimeInMillis(instant.getOffsetMillis());
    return toTimeX(calendar,this.timeFields);
  }
  
  /**
   * Convert an Instant into a time expression using this Chronology and 
   *   the specified TimeFields 
   * 
   * @param timeZone
   * @param locale
   * @param instant
   * @return
   */
  public TimeX toTimeX
    (TimeZone timeZone,Locale locale,Instant instant,TimeField[] fields)
  {

    Calendar calendar=newCalendarImpl(timeZone,locale);
    calendar.setTimeInMillis(instant.getOffsetMillis());
    return toTimeX(calendar,fields);
  }
  
  /**
   * Convert a time expression to an instant using this Chronology
   * 
   * @param timeZone
   * @param locale
   * @param time
   * @return
   */
  public Instant toInstant(TimeZone timeZone,Locale locale,TimeX time)
  { 
    Calendar calendar=newCalendarImpl(timeZone,locale);
    setCalendarFields(calendar,time);
    return new Instant(calendar.getTimeInMillis());
  }
  
  private TimeX toTimeX(Calendar calendar,TimeField[] timeFields)
  {
    TimeX.TimeValue[] timeValues
      =new TimeX.TimeValue[timeFields.length];
  
    int i=0;
    for (TimeField timeField: timeFields)
    { timeValues[i++]=newTimeValue(calendar,timeField);
    }
    return new TimeX(timeValues);    
  }
  
  

  
  private TimeX.TimeValue newTimeValue(Calendar calendar,TimeField field)
  { return new TimeX.TimeValue(field,calendar.get(field.calendarField));
  }
  
  class FieldIterator
    implements Iterator<Instant>
  {

    private final Calendar calendar;
    private final int calendarField;
    private Instant next;
    private int interval;
    private long stopMillis;
    
    FieldIterator(Calendar calendar,TimeField field,int interval,Instant stop)
    { 
      this.calendar=calendar;
      this.interval=interval;
      if (interval==0)
      { 
        throw new IllegalArgumentException
          ("Cannot iterate a Calendar field using an interval of 0");
      }
      next=new Instant(calendar.getTimeInMillis());
      calendarField=field.calendarField;
      stopMillis=stop.getOffsetMillis();
      log.fine("Iterating "+calendar);
    }
      
      
    @Override
    public boolean hasNext()
    { return next!=null;
    }

    @Override
    public Instant next()
    {
      if (next==null)
      { throw new NoSuchElementException();
      }
      Instant ret=next;
      
      
      calendar.add(calendarField,interval);
      long millis=calendar.getTimeInMillis();
      log.fine("Calendar time is "+millis);
      
      if ( (interval<0 && millis<=stopMillis)
           || (interval>0 && millis>=stopMillis)
         )
      { next=null;
      }
      else
      { next=new Instant(millis);
      }
      
      return ret;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("Time Iterator is read-only");
    }
    
  }
  
  
}
