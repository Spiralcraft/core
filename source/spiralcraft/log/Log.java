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
  public static final Level FINE=Level.FINE;
  public static final Level TRACE=Level.TRACE;
  public static final Level DEBUG=Level.DEBUG;
  public static final Level CONFIG=Level.CONFIG;
  public static final Level INFO=Level.INFO;
  public static final Level WARNING=Level.WARNING;
  public static final Level SEVERE=Level.SEVERE;
  public static final Level OFF=Level.OFF;
  
  /**
   * <p>Indicate whether this Log will log events as detailes as the specified
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
  
  public void log(Level level,String message);
  
  public void log(Level level,String message,Throwable thrown);
  
  public void log(Level level,String message,Throwable thrown,int stackOffset);
  
  public void log(Event event);
  
}
