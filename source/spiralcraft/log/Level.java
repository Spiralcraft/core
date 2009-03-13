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
 * <p>Represents the granularity of a Log event, for the purposes of filtering
 *   log messages. The supported values are, in order of verbosity:
 * </p>
 * 
 * <ul>
 *   <li>ALL<p>All messages (used as a filter level)</p></li>
 *   <li>FINE<p>A potentially voluminous amount of fine level
 *      execution detail</p></li>
 *   <li>TRACE<p>A coarse level of execution detail</p></li>
 *   <li>DEBUG<p>Key execution points useful for debugging</p></li>
 *   <li>CONFIG<p>Configuration event details</p></li>
 *   <li>INFO<p>Various high level events typically associated with status
 *    changes</p></li>
 *   <li>WARNING<p>Abnormal events or situations that have been successfully
 *    handled.</p></li>
 *   <li>SEVERE<p>Abnormal events or situations that have not been resolved
 *    and that may result in systemic trouble</p></li>
 * </ul>
 * @author mike
 *
 */
public class Level
{
  
  public static final Level ALL=new Level("ALL",0);
  public static final Level FINE=new Level("FINE",1000);
  public static final Level TRACE=new Level("TRACE",2000);
  public static final Level DEBUG=new Level("DEBUG",3000);
  public static final Level CONFIG=new Level("CONFIG",4000);
  public static final Level INFO=new Level("INFO",5000);
  public static final Level WARNING=new Level("WARNING",6000);
  public static final Level SEVERE=new Level("SEVERE",7000);
  public static final Level OFF=new Level("OFF",Integer.MAX_VALUE);
  
  private final String name;
  private final int value;
  
  protected Level(String name,int value)
  { 
    this.name=name;
    this.value=value;
  }
  
  /**
   * <p>Check to see whether messages of a specified Level will be logged in
   *   a logger of this Level
   * </p>
   * @param level
   * @return Whether this Level includes events of the specified Level.
   */
  public boolean canLog(Level level)
  { return value<=level.value;
  }
  
  public String getName()
  { return name;
  }
  
  public int getValue()
  { return value;
  }
  
}
