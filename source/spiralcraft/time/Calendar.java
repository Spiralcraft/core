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

import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>A abstraction that understands how to calculate time expressions
 *   for a given Chronology, Locale, and TimeZone.
 * </p>
 * 
 * <p>A Calendar is thread-safe and contains no internal synchronization.
 * </p>
 * 
 * @author mike
 *
 */
public class Calendar
{

  public static final Calendar DEFAULT
    =new Calendar();
  
  protected final Locale locale;
  protected final Chronology chronology;
  protected final TimeZone timeZone;
  
  /**
   * Construct a Calendar with the specified Chronology, TimeZone and Locale
   * 
   * @param chronology
   * @param timeZone
   * @param locale
   */
  public Calendar(Chronology chronology,TimeZone timeZone,Locale locale)
  { 
    this.chronology=chronology;
    this.timeZone=timeZone;
    this.locale=locale;
  }
    
  /**
   * Construct a Calendar with the specified TimeZone, the standard chronology
   *   and the default Locale for the JVM installation.
   *   
   * @param timeZone
   */
  public Calendar(TimeZone timeZone)
  {
    this.timeZone=timeZone;
    this.chronology=Chronology.GREGORIAN;
    this.locale=Locale.getDefault();
  }
  
  /**
   * Construct a Calendar with the the standard chronology
   *   and the default TimeZone and Locale for the JVM installation.
   *   
   * @param timeZone
   */
  public Calendar()
  { this(TimeZone.getDefault());
  }
  
  /**
   * Iterate through a set of intervals of the specified TimeField between
   *   the specified start time and the specified stop time.
   * 
   * @param start
   * @param field
   * @param interval
   * @param stop
   * @return
   */
  public Iterator<Instant> iterate
    (Instant start
    ,TimeField field
    ,int interval
    ,Instant stop
    )
  { return chronology.iterate(timeZone,locale,start,field,interval,stop);
  }
  
  /**
   * Compute a new Instant by adding a value to the specified TimeField.
   * 
   * @param start
   * @param field
   * @param amount
   * @return
   */
  public Instant add(Instant start,TimeField field,int amount)
  { return chronology.add(timeZone,locale,start,field,amount);
  }
  
  public Instant add(Instant start,Duration duration)
  { return chronology.add(timeZone,locale,start,duration);
  }
  
  public Date add(Date start,Duration duration)
  { 
    if (start==null || duration==null)
    { return null;
    }
    return add(Instant.fromDate(start),duration).toDate();
  }
  
  public TimeX add(TimeX start,TimeField field,int amount)
  { return chronology.add(timeZone,locale,start,field,amount);
  }

  public Instant subtract(Instant end,Duration duration)
  { return chronology.subtract(timeZone,locale,end,duration);
  }
  
  public Date subtract(Date end,Duration duration)
  { 
    if (end==null || duration==null)
    { return null;
    }
    return subtract(Instant.fromDate(end),duration).toDate();
  }

  public Duration subtract(Instant end,Instant start)
  { return chronology.subtract(timeZone,locale,end,start);
  }

  public Duration subtract(Instant end,Instant start,Chronom[] units)
  { return chronology.subtract(timeZone,locale,end,start,units);
  }
  
  /**
   * <p>Rebase a TimeX from an original time to within a single unit of 
   *   a destination Instant. Used to move time expressions between absolute
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
   * @param original
   * @param dest
   * @param field
   * @param offset
   * @return
   */
  public TimeX rebase(TimeX original,Instant dest,TimeField field,int offset)
  { return chronology.rebase(timeZone,locale,original,dest,field,offset);
  }


  /**
   * Get the value of a single TimeField from an Instant with respct to the
   *   time zone and locale of this Calendar.
   * 
   * @param instant
   * @param field
   * @return
   */
  public int getField(Instant instant,TimeField field)
  { return chronology.getField(timeZone,locale,instant,field);
  }
  
  /**
   * Convert an Instant to a TimeX using the standard fields of the 
   *   Chronology.
   * 
   * @param instant
   * @return
   */
  public TimeX toTimeX(Instant instant)
  { return chronology.toTimeX(timeZone,locale,instant);
  }
  
  
  /**
   * Convert an Instant to a TimeX using the specified fields
   * 
   * @param instant
   * @param fields
   * @return
   */
  public TimeX toTimeX(Instant instant,TimeField[] fields)
  { return chronology.toTimeX(timeZone,locale,instant,fields);
  }
  
  public Instant toInstant(TimeX time)
  { return chronology.toInstant(timeZone,locale,time);
  }

  /**
   * Return the first Instant of the period that contains the
   *   specified instant.
   * 
   * @param now
   * @param period
   * @return
   */
  public Instant startOfPeriod(Instant instant,TimeField period)
  { return chronology.startOfPeriod(timeZone,locale,instant,period);
  }

  /**
   * Return the first Instant of the period after the period that contains
   *   the specified instant.
   * 
   * @param now
   * @param period
   * @return
   */
  public Instant startOfNextPeriod(Instant instant,TimeField period)
  { return chronology.startOfNextPeriod(timeZone,locale,instant,period);
  }
  
//  public abstract Instant add(Instant first,Duration duration);
//  
//  public abstract Duration subtract(Instant reference,Instant target);

  
}
