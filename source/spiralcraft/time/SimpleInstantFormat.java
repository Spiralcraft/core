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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleInstantFormat
{

  private final SimpleDateFormat format;
  
  public SimpleInstantFormat(String formatString)
  { this(Calendar.DEFAULT,formatString);
  }
  
  public SimpleInstantFormat(Calendar calendar,String formatString)
  {
    this.format=new SimpleDateFormat(formatString,calendar.locale);
    this.format.setTimeZone(calendar.timeZone);
    
  }
  
  public Instant parse(String input)
    throws ParseException
  { return new Instant(format.parse(input).getTime());
  }
  
  public String format(Instant instant)
  { return format.format(new Date(instant.getOffsetMillis()));
  }
}
