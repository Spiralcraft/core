//
// Copyright (c) 1998,2008 Michael Toth
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

import spiralcraft.time.Duration;
import spiralcraft.time.StandardDurationFormat;

import java.text.ParseException;

/**
 * <p>A StringConverter for Duration objects which uses a 
 *   java.text.SimpleDurationFormat and an associated format string to perform
 *   the conversion.
 * </p>
 * 
 * <p>The default format string is yyyy-MM-dd HH:mm:ss.S Z
 * </p>
 * 
 * @author mike
 *
 */
public final class DurationToString
  extends StringConverter<Duration>
{
  
  
  private volatile ThreadLocal<StandardDurationFormat> formatLocal;
  

  
  public DurationToString()
  { newFormat();
  }
  


  
  private void newFormat()
  {
    formatLocal
      =new ThreadLocal<StandardDurationFormat>()
      {
        @Override
        public StandardDurationFormat initialValue()
        { return new StandardDurationFormat();
        }
      };
  }
  
  @Override
  public String toString(Duration val)
  { 
    if (val==null)
    { return null;
    }
    return formatLocal.get().format(val);
  }
    

  @Override
  public Duration fromString(String val)
  { 
    if (val==null)
    { return null;
    }
    else
    { 
      try
      { return formatLocal.get().parse(val);
      }
      catch (ParseException x)
      { 
        throw new IllegalArgumentException
          ("Error parsing Duration: "+val,x);
      }
    }

  }
}
