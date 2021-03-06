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

import spiralcraft.time.Instant;
import spiralcraft.time.SimpleInstantFormat;

import java.text.ParseException;

/**
 * <p>A StringConverter for Instant objects which uses a 
 *   java.text.SimpleInstantFormat and an associated format string to perform
 *   the conversion.
 * </p>
 * 
 * <p>The default format string is yyyy-MM-dd HH:mm:ss.S Z
 * </p>
 * 
 * @author mike
 *
 */
public final class InstantToString
  extends StringConverter<Instant>
{
  
  private String formatString="yyyy-MM-dd HH:mm:ss.S Z";
  
  private volatile ThreadLocal<SimpleInstantFormat> formatLocal;
  
  
  

  public InstantToString(String formatString)
  { setFormatString(formatString);
  }
  
  public InstantToString()
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
      =new ThreadLocal<SimpleInstantFormat>()
      {
        @Override
        public SimpleInstantFormat initialValue()
        { return new SimpleInstantFormat(newFormatString);
        }
      };
  }
  
  @Override
  public String toString(Instant val)
  { 
    if (val==null)
    { return null;
    }
    return formatLocal.get().format(val);
  }
    

  @Override
  public Instant fromString(String val)
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
          ("Error parsing date using format '"+formatString+"': "+val,x);
      }
    }

  }
}
