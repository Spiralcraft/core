//
// Copyright (c) 1998,2005 Michael Toth
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

import java.text.DateFormat;
import java.text.ParseException;

import java.util.Date;


/**
 * Maintains a formatted representation of the approximate system time,
 *   as determined by the Clock associated with this object. A new
 *   formatted representation is generated when the prior one is considered
 *   out of date with respect to the specified precision.
 */
public class ClockFormat
{
  private final Clock _clock;
  private final DateFormat _format;
  private final int _precisionMillis;
  private String _formattedTime;
  private long _lastTime;

  /**
   * Construct a ClockFormat using the standard clock
   */
  public ClockFormat(DateFormat format,int precisionMillis)
  { this(Clock.instance(),format,precisionMillis);
  }

  /**
   * Construct a ClockFormat using  a specific Clock
   */
  public ClockFormat(Clock clock,DateFormat format,int precisionMillis)
  { 
    _clock=clock;
    _format=format;
    _precisionMillis=precisionMillis;
  }

  /**
   * Obtain the approximate time in milliseconds.
   */
  public final synchronized String approxTimeFormatted()
  { 
    long time=_clock.approxTimeMillis();
    if (time-_lastTime>_precisionMillis)
    { 
      _formattedTime=_format.format(new Date(time));
      _lastTime=time;
    }
    return _formattedTime;
  }


}


