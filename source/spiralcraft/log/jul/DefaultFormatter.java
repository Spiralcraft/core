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
package spiralcraft.log.jul;

import java.text.SimpleDateFormat;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import java.util.Date;

public class DefaultFormatter
  extends Formatter
{
  private SimpleDateFormat _dateFormat
    =new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSSZ");
  private String _cr=System.getProperty("line.separator");

  @Override
  public synchronized String format(LogRecord record)
  { 
    StringBuffer out=new StringBuffer();
    out.append(_dateFormat.format(new Date(record.getMillis())));
    out.append(" ");
    out.append(record.getLevel().getName());
    out.append(" ");
    out.append(Long.toString(record.getLongThreadID()));
    out.append(" ");
    out.append(record.getLoggerName());
    out.append(" (");
    out.append(record.getSourceClassName());
    out.append(".");
    out.append(record.getSourceMethodName());
    out.append(")");
    out.append(_cr);
    out.append("  ");
    out.append(formatMessage(record));
    out.append(_cr);
    if (record.getThrown()!=null)
    { 
      out.append(record.getThrown().toString());
      out.append(_cr);
    }
    return out.toString();
  }
  

}
