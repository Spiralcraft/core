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

import java.text.DecimalFormat;
import java.text.ParseException;


/**
 * <p>A StringConverter for Decimal numbers which uses a 
 *   java.text.DecimalFormat and an associated format string to perform
 *   the conversion.
 * </p>
 * 
 * <p>The default format string is yyyy-MM-dd HH:mm:ss.S Z
 * </p>
 * @author mike
 *
 */
public abstract class NumberToDecimalString<X extends Number>
  extends StringConverter<X>
{
  
  private String formatString;
  
  private volatile ThreadLocal<DecimalFormat> formatLocal;
  
  /**
   * Create a StringConverter that converts a formatted decimal number
   *            
   * @param formatString
   */
  public NumberToDecimalString(String formatString)
  { setFormatString(formatString);
  }

  
  public NumberToDecimalString()
  { newFormat(formatString);
  }
  
  private void setFormatString(String formatString)
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
      =new ThreadLocal<DecimalFormat>()
      {
        @Override
        public DecimalFormat initialValue()
        { 
          if (newFormatString!=null)
          { return new DecimalFormat(newFormatString);
          }
          else
          { return new DecimalFormat();
          }
        }
      };
  }
  
  @Override
  public String toString(Number val)
  { 
    if (val==null)
    { return null;
    }
    return formatLocal.get().format(val);
  }
    
  public abstract X coerce(Number val);
  
  @Override
  public X fromString(String val)
  { 
    if (val==null)
    { return null;
    }
    else
    { 
      try
      { return coerce(formatLocal.get().parse(val));
      }
      catch (ParseException x)
      { 
        throw new IllegalArgumentException
          ("Error parsing number using format '"+formatString+"': "+val,x);
      }
    }

  }
}