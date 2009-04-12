package spiralcraft.log.jul;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import spiralcraft.log.ClassLog;
import spiralcraft.log.ContextLog;
import spiralcraft.log.Event;
import spiralcraft.util.Path;

public class HandlerAdapter
  extends Handler
{

  private final ContextLog log
    =ClassLog.getInstance(HandlerAdapter.class);
  
  private SimpleFormatter formatter=new SimpleFormatter();
  
  @Override
  public void close()
    throws SecurityException
  {

  }

  @Override
  public void flush()
  {

  }

  @Override
  public void publish(
    LogRecord record)
  {

    Event event
      =new Event
      (Thread.currentThread().getStackTrace()[2]
      ,new Path(record.getLoggerName(),'.')
      ,translateLevel(record.getLevel())
      ,formatter.formatMessage(record)
      ,record.getThrown()
      ,translateDetails(record)
      );
    log.log(event);
  }

  private Object[] translateDetails(LogRecord record)
  { 
    record.getLevel();
    return null;
  }
    
  private spiralcraft.log.Level translateLevel(java.util.logging.Level level)
  {
    int jlevel=level.intValue();
    
    if (jlevel>=java.util.logging.Level.OFF.intValue())
    { return spiralcraft.log.Level.OFF;
    }
    else if (jlevel>=java.util.logging.Level.SEVERE.intValue())
    { return spiralcraft.log.Level.SEVERE;
    }
    else if (jlevel>=java.util.logging.Level.WARNING.intValue())
    { return spiralcraft.log.Level.WARNING;
    }
    else if (jlevel>=java.util.logging.Level.INFO.intValue())
    { return spiralcraft.log.Level.INFO;
    }
    else if (jlevel>=java.util.logging.Level.CONFIG.intValue())
    { return spiralcraft.log.Level.CONFIG;
    }
    else if (jlevel>=java.util.logging.Level.FINE.intValue())
    { return spiralcraft.log.Level.DEBUG;
    }
    else if (jlevel>=java.util.logging.Level.FINER.intValue())
    { return spiralcraft.log.Level.TRACE;
    }
    else if (jlevel>=java.util.logging.Level.FINE.intValue())
    { return spiralcraft.log.Level.FINE;
    }
    else
    { return spiralcraft.log.Level.ALL;
    }
  }
}
