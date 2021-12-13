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


/**
 * <p>A means for application code to record programmatic events. 
 * </p>
 * 
 * <p>This interface is designed to be a stable fascade to include in
 *   source code and can be mapped to a variety of different logging 
 *   infrastructure implementations. 
 * </p>
 * 
 * <p>A given instance of a Log may incorporate contextual information by
 *   simply delegating an Event to a Log scoped to a broader context.
 * </p>
 * 
 * <p>Static fields of Level which define standard levels are duplicated here
 *   for interface fluency.
 * </p>
 * 
 * @author mike
 *
 */
public interface Log
{
  public static final Level ALL=Level.ALL;
  public static final Level FINEST=Level.FINEST;
  public static final Level FINER=Level.FINER;
  public static final Level FINE=Level.FINE;
  public static final Level TRACE=Level.TRACE;
  public static final Level DEBUG=Level.DEBUG;
  public static final Level CONFIG=Level.CONFIG;
  public static final Level INFO=Level.INFO;
  public static final Level WARNING=Level.WARNING;
  public static final Level SEVERE=Level.SEVERE;
  public static final Level OFF=Level.OFF;
  
  /**
   * <p>Indicate whether this Log will log events as detailed as the specified
   *   Level. Application code should check this before calling log(...).
   *   
   * </p>
   * 
   * <p>If events are filtered by the Log, it is permissible for the log to 
   *   provide a count of such filtered events, because a large number of
   *   generated but filtered log messages may be detrimental to performance.
   * </p>
   * 
   * @param level
   * @return
   */
  public boolean canLog(Level level);
  
  public void log(Level level,String message,Throwable thrown,int stackOffset);
  
  public void log(Event event);
  
  public default void log(Level level,String message)
  { log(level,message,null,1);
  }

  public default void log(Level level,String message,Throwable thrown)
  { log(level,message,thrown,1);
  }  
  
  public default boolean canLogSevere()
  { return canLog(Level.SEVERE);
  }
  
  public default boolean canLogWarning()
  { return canLog(Level.WARNING);
  }

  public default boolean canLogInfo()
  { return canLog(Level.INFO);
  }

  public default boolean canLogConfig()
  { return canLog(Level.CONFIG);
  }

  public default boolean canLogDebug()
  { return canLog(Level.DEBUG);
  }

  public default boolean canLogTrace()
  { return canLog(Level.TRACE);
  }

  public default boolean canLogFine()
  { return canLog(Level.FINE);
  }

  public default boolean canLogFiner()
  { return canLog(Level.FINER);
  }

  public default boolean canLogFinest()
  { return canLog(Level.FINEST);
  }

  public default void severe(String message)
  { log(Level.SEVERE,message,null,1);
  }

  public default void severe(String message,Throwable x)
  { log(Level.SEVERE,message,x,1);
  }

  public default void severe(String message,Throwable x,int stackOffset)
  { log(Level.SEVERE,message,x,stackOffset+1);
  }

  public default void warning(String message)
  { log(Level.WARNING,message,null,1);
  }

  public default void warning(String message,Throwable x)
  { log(Level.WARNING,message,x,1);
  }

  public default void warning(String message,Throwable x,int stackOffset)
  { log(Level.WARNING,message,x,stackOffset+1);
  }

  public default void info(String message)
  { log(Level.INFO,message,null,1);
  }

  public default void info(String message,Throwable x)
  { log(Level.INFO,message,x,1);
  }

  public default void info(String message,Throwable x,int stackOffset)
  { log(Level.INFO,message,x,stackOffset+1);
  }

  public default void config(String message)
  { log(Level.CONFIG,message,null,1);
  }

  public default void config(String message,Throwable x)
  { log(Level.CONFIG,message,x,1);
  }

  public default void config(String message,Throwable x,int stackOffset)
  { log(Level.CONFIG,message,x,stackOffset+1);
  }

  public default void debug(String message)
  { log(Level.DEBUG,message,null,1);
  }

  public default void debug(String message,Throwable x)
  { log(Level.DEBUG,message,x,1);
  }

  public default void debug(String message,Throwable x,int stackOffset)
  { log(Level.DEBUG,message,x,stackOffset+1);
  }

  public default void trace(String message)
  { log(Level.TRACE,message,null,1);
  }

  public default void trace(String message,Throwable x)
  { log(Level.TRACE,message,x,1);
  }

  public default void trace(String message,Throwable x,int stackOffset)
  { log(Level.TRACE,message,x,stackOffset+1);
  }

  public default void fine(String message)
  { log(Level.FINE,message,null,1);
  }

  public default void fine(String message,Throwable x)
  { log(Level.FINE,message,x,1);
  }

  public default void fine(String message,Throwable x,int stackOffset)
  { log(Level.FINE,message,x,stackOffset+1);
  }

  public default void finer(String message)
  { log(Level.FINER,message,null,1);
  }

  public default void finer(String message,Throwable x)
  { log(Level.FINER,message,x,1);
  }

  public default void finer(String message,Throwable x,int stackOffset)
  { log(Level.FINER,message,x,stackOffset+1);
  }

  public default void finest(String message)
  { log(Level.FINEST,message,null,1);
  }

  public default void finest(String message,Throwable x)
  { log(Level.FINEST,message,x,1);
  }

  public default void finest(String message,Throwable x,int stackOffset)
  { log(Level.FINEST,message,x,stackOffset+1);
  }
  
}
