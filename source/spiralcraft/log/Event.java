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
package spiralcraft.log;

import spiralcraft.time.Clock;
import spiralcraft.util.Path;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * <p>Encapsulates a logged event.
 * </p>
 * 
 */
public class Event
{
  private static DateFormat DEFAULT_DATE_FORMAT
    =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

  private static final Path DEFAULT_CONTEXT=new Path("",'/');
  
  private final Level level;
  private final String message;
  private final String threadName;
  private final Path context;
  private final long threadId;
  private final long time;
  private final Throwable thrown;
  private final StackTraceElement callSite;

  public static Event create
    (Path context
    ,Level level
    ,String message
    )
  { 
    return new Event
      (Thread.currentThread().getStackTrace()[2]
      ,context
      ,level
      ,message
      ,null
      ,null
      );
  }
  
  public static Event create
    (Path context
    ,Level level
    ,String message
    ,Throwable thrown
    )
  { 
    return new Event
      (Thread.currentThread().getStackTrace()[2]
      ,context
      ,level
      ,message
      ,thrown
      ,null
      );
  }

  public static Event create
    (Path context
    ,Level level
    ,String message
    ,Throwable thrown
    ,Object[] details
    )
  { 
    return new Event
      ( Thread.currentThread().getStackTrace()[2]
      ,context
      ,level
      ,message
      ,thrown
      ,details
      );
  }
  
  public Event
    (StackTraceElement callSite
    ,Path context
    ,Level level
    ,String message
    ,Throwable thrown
    ,Object[] details
    )
  { 
    this.level=level;
    if (details!=null)
    { this.message=MessageFormat.format(message, details);
    }
    else
    { this.message=message;
    }
    time=Clock.instance().approxTimeMillis();
    
    this.threadName=Thread.currentThread().getName();
    this.threadId=Thread.currentThread().getId();
    if (context==null)
    { this.context=DEFAULT_CONTEXT;
    }
    else
    { this.context=context;
    }
    this.thrown=thrown;
    this.callSite=callSite;
  }

  public Level getLevel()
  { return level;
  }

  public String getMessage()
  { return message;
  }

  public String getThreadName()
  { return threadName;
  }

  public long getThreadId()
  { return threadId;
  }
  
  public long getTime()
  { return time;
  }

  public Path getContext()
  { return context;
  }

  public StackTraceElement getCallSite()
  { return callSite;
  }
  
  public Throwable getThrown()
  { return thrown;
  }
  
  @Override
  public String toString()
  {
    final StringBuffer out=new StringBuffer();
    out.append("[");
    synchronized (DEFAULT_DATE_FORMAT)
    { out.append(DEFAULT_DATE_FORMAT.format(new Date(time)));
    }
    out.append("]");
    out.append(" ");
    out.append(threadId+"("+threadName+")");
    out.append(" ");
    out.append(level.getName());
    out.append(" ");
    out.append(context.format("/"));
    out.append(" ");
    out.append(callSite.getClassName())
      .append("."+callSite.getMethodName())
      .append(" ("+callSite.getFileName()+":"+callSite.getLineNumber()+")");
    
    if (message!=null)
    { out.append("\r\n    "+message);
    }
    if (thrown!=null)
    { 
      out.append("\r\n    "+thrown.toString());
      StringWriter trace=new StringWriter();
      thrown.printStackTrace(new PrintWriter(trace,true));
      out.append("\r\n    "+trace.toString());
    }
    return out.toString();
  }
}
