//
// Copyright (c) 1998,2015 Michael Toth
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
package spiralcraft.util.string;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import spiralcraft.time.Calendar;
import spiralcraft.time.Instant;
import spiralcraft.time.TimeField;

/**
 * <p>A StringConverter for Date objects which uses a 
 *   java.text.SimpleDateFormat and an associated format string to perform
 *   the conversion.
 * </p>
 * 
 * <p>The default format string is yyyy-MM-dd HH:mm:ss.S Z
 * </p>
 * @author mike
 *
 */
public final class DateToString
  extends StringConverter<Date>
{
  
  private String formatString="yyyy-MM-dd HH:mm:ss.S Z";
  
  private volatile ThreadLocal<SimpleDateFormat> formatLocal;
  private TimeField precision=TimeField.MILLISECOND;
  private Calendar calendar;
  private boolean roundUp;
  
  /**
   * Create a StringConverter that converts a date/time with courser than
   *   millisecond precision and rounds down or up to the beginning or end
   *   of the period represented by the specific precision.
   *   
   * Rounding up will return the millisecond before the start of the next
   *   increment of the specific precision.
   *      
   * @param formatString
   * @param precision
   * @param roundUp
   */
  public DateToString(String formatString,TimeField precision,boolean roundUp)
  {
    this.precision=precision;
    this.roundUp=roundUp;
    this.calendar=Calendar.DEFAULT;
    setFormatString(formatString);
  }

  public DateToString(String formatString)
  { setFormatString(formatString);
  }
  
  public DateToString()
  { newFormat(formatString);
  }
  
  public void setFormatString(String formatString)
  { 
    this.formatString=formatString;
    newFormat(formatString);
  }

  public String getFormatString()
  { return formatString;
  }
  
  private void newFormat(final String newFormatString)
  {
    formatLocal
      =new ThreadLocal<SimpleDateFormat>()
      {
        @Override
        public SimpleDateFormat initialValue()
        {
          return new SimpleDateFormat(newFormatString);
        }
      };
  }
  
  @Override
  public String toString(Date val)
  { 
    if (val==null)
    { return null;
    }
    return formatLocal.get().format(val);
  }
    

  @Override
  public Date fromString(String val)
  { 
    if (val==null)
    { return null;
    }
    else
    { 
      try
      { 
        if (precision==TimeField.MILLISECOND)
        { return formatLocal.get().parse(val);
        }
        else
        {
          Date date=formatLocal.get().parse(val);
          if (!roundUp)
          { return calendar.startOfPeriod(Instant.fromDate(date),precision).toDate();
          }
          else
          { 
            return new Date
              (calendar.startOfNextPeriod
                (Instant.fromDate(date)
                ,precision
                ).getOffsetMillis()-1
              );
          }
        }
      }
      catch (ParseException x)
      { 
        throw new IllegalArgumentException
          ("Error parsing date using format '"+formatString+"': "+val,x);
      }
    }

  }
  
  public String toString()
  { return super.toString()+": format="+formatString;
  }
}