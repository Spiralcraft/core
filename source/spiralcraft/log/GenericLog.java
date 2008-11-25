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

import java.util.LinkedList;

import spiralcraft.util.Path;

/**
 * <p>Generic implementation of a Log
 * </p>
 * 
 * @author mike
 *
 */
public class GenericLog
  implements Log
{

  protected Level level=ALL;
  protected Path context;
  protected LinkedList<EventHandler> handlers=new LinkedList<EventHandler>();
  private Log parent;
  
  public GenericLog()
  { parent=ContextLog.getInstance();
  }
  
  public GenericLog(Log parent)
  { this.parent=parent;
  }
  
  public void setHandlers(EventHandler ... handlers)
  { 
    this.handlers=new LinkedList<EventHandler>();
    for (EventHandler handler:handlers)
    { this.handlers.add(handler);
    }
  }
  
  @Override
  public boolean canLog(Level level)
  { return this.level.canLog(level);
  }

  @Override
  public void log(Level level,String message)
  { 
    if (this.level.canLog(level))
    { 
      log
        (new Event
          (Thread.currentThread().getStackTrace()[2]
          ,context
          ,level
          ,message
          ,null
          ,null
          )
        );
    }
    
  }

  @Override
  public void log(
    Level level,
    String message,
    Throwable thrown)
  {
    if (this.level.canLog(level))
    { 
      log
        (new Event
          (Thread.currentThread().getStackTrace()[2]
          ,context
          ,level
          ,message
          ,thrown
          ,null
          )
        );
    }
    
  }

  protected void log(
    Level level,
    String message,
    Throwable thrown
    ,int traceDepth
    )
  {
    if (this.level.canLog(level))
    { 
      log
        (new Event
          (Thread.currentThread().getStackTrace()[2+traceDepth]
          ,context
          ,level
          ,message
          ,thrown
          ,null
          )
        );
    }
    
  }
  
  @Override
  public void log(Event event)
  {
    if (level.canLog(event.getLevel()))
    {
      if (handlers!=null)
      { 
        for (EventHandler handler: handlers)
        { handler.handleEvent(event);
        }
      }
    }
    if (parent!=null)
    { parent.log(event);
    }
  }

}
