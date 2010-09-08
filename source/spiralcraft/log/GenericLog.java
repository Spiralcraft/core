//
// Copyright (c) 1998,2009 Michael Toth
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

import java.util.Iterator;
import java.util.LinkedList;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.util.Path;

/**
 * <p>Generic implementation of a Log.
 * </p>
 * 
 * <p>A level can be specified to limit reporting detail and the associated
 *   performance cost.
 * </p>
 * 
 * <p>Will forward events to the optional parent log
 * </p>

 * <p>A list of handlers can be specified to receive events that match the
 *   specified detail level. If there are no specified handlers, and no
 *   parent log, the default ConsoleHandler() will send events to
 *   the error stream of the spiralcraft.exec.ExecutionContext associated
 *   with the logging thread.
 * </p>
 * 
 * 
 * @author mike
 *
 */
public class GenericLog
  implements Log,Lifecycle
{
  private static EventHandler DEFAULT_HANDLER=new ConsoleHandler();

  protected Level level=ALL;
  protected Path context;
  protected LinkedList<EventHandler> handlers=new LinkedList<EventHandler>();
  private Log parent;
  
  public GenericLog()
  { 
  }
  
  public GenericLog(Log parent)
  { this.parent=parent;
  }
  
  public void addHandler(EventHandler handler)
    throws LifecycleException
  { 
    if (handlers==null)
    { handlers=new LinkedList<EventHandler>();
    }
    synchronized(this.handlers)
    {
      this.handlers.add(handler);
      handler.start();
    }
  }
  
  
  /**
   * Remove the specified EventHandler from the notification list
   * 
   * @throws LifecycleException
   */
  public void removeHandler(EventHandler handler)
    throws LifecycleException
  {
    synchronized (handlers)
    {
      Iterator<EventHandler> it=handlers.iterator();
      while (it.hasNext())
      {
        EventHandler comp=it.next();
        if (handler==comp)
        { 
          it.remove();
          handler.stop();
          break;
        }
      }
    }
    
  }
  
  public void setHandlers(EventHandler ... handlers)
  { 
    this.handlers=new LinkedList<EventHandler>();
    synchronized (this.handlers)
    {
      for (EventHandler handler:handlers)
      { this.handlers.add(handler);
      }
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

  @Override
  public void log(
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
  /**
   * Dispatches a log event to handlers and the parent log 
   */
  public void log(Event event)
  {

    if (level.canLog(event.getLevel()))
    {
      if (handlers!=null && handlers.size()>0)
      { 
        synchronized (handlers)
        {
          for (EventHandler handler: handlers)
          { handler.handleEvent(event);
          }
        }
      }
      else if (parent==null)
      { DEFAULT_HANDLER.handleEvent(event);
      }
    }
    if (parent!=null)
    { parent.log(event);
    }
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    if (handlers!=null)
    { 
      for (EventHandler handler: handlers)
      { handler.start();
      }
    }
      
  }
  
  @Override
  public void stop()
    throws LifecycleException
  { 
    if (handlers!=null)
    { 
      for (EventHandler handler: handlers)
      { handler.stop();
      }
    }   
  }

}
