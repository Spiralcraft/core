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
package spiralcraft.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;


import java.util.Date;

public class DefaultFormatter
  implements Formatter
{
  private SimpleDateFormat _dateFormat
    =new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSSZ");
  private String _cr=System.getProperty("line.separator");

  @Override
  public synchronized String format(Event event)
  { 
    StringBuffer out=new StringBuffer();
    out.append("[");
    out.append(_dateFormat.format(new Date(event.getTime())));
    out.append("]");
    out.append(" ");
    out.append(event.getLevel().getName());
    out.append(" ");
    out.append(Long.toString(event.getThreadId()));
    out.append(":");
    out.append(event.getThreadName());
    out.append(" (");
    out.append(event.getContext().format(":"));
    out.append("): ");
    if (Level.DEBUG.getValue()>=event.getLevel().getValue())
    {
      out.append(_cr+"        ");
      out.append(event.getCallSite().getClassName());
      out.append(".");
      out.append(event.getCallSite().getMethodName());
      out.append(" (");
      out.append(event.getCallSite().getFileName());
      out.append(":"+event.getCallSite().getLineNumber());
      out.append(")");
      out.append(_cr);
      out.append("  ");
    }
    out.append(event.getMessage());
    if (event.getThrown()!=null)
    { 
      out.append(_cr);
      StringWriter writer=new StringWriter();
      event.getThrown().printStackTrace(new PrintWriter(writer,true));
      out.append(writer.toString());
    }
    return out.toString();
  }
  

}
