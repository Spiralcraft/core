package spiralcraft.log;

import java.text.SimpleDateFormat;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import java.util.Date;

public class DefaultFormatter
  extends Formatter
{
  private SimpleDateFormat _dateFormat
    =new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SZ");
  private String _cr=System.getProperty("line.separator");

  public synchronized String format(LogRecord record)
  { 
    StringBuffer out=new StringBuffer();
    out.append(_dateFormat.format(new Date(record.getMillis())));
    out.append(" ");
    out.append(record.getLevel().getName());
    out.append(" ");
    out.append(Integer.toString(record.getThreadID()));
    out.append(" ");
    out.append(record.getLoggerName());
    out.append(_cr);
    if (record.getLevel().intValue()<=Level.FINE.intValue())
    { 
      out.append(record.getSourceClassName());
      out.append(".");
      out.append(record.getSourceMethodName());
      out.append(_cr);
    }
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
