//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.task;

import java.util.HashSet;
import java.util.List;

/**
 * <p>A task made up of a number of subtasks. Typical implementations are
 *   the SerialTask or the ParallelTask
 * </p>
 * 
 * @author mike
 *
 */
public abstract class MultiTask<Tsubtask,Tresult>
  extends AbstractTask<Tresult>
  implements TaskListener
{
  
  protected final List<Tsubtask> subtasks;
  protected final HashSet<Task> runningTasks
    =new HashSet<Task>();
  
  protected final Object monitor=new Object();  
  
  public MultiTask(final List<Tsubtask> subtasks)
  { this.subtasks=subtasks;
  }
  
  public List<Tsubtask> getSubtasks()
  { return subtasks;
  }
  
  
  protected void subtaskStarting(Task task)
  { 
    synchronized (monitor)
    { runningTasks.add(task);
    }
  }
  
  protected void subtaskCompleted(Task task)
  { 
    if (debug)
    { log.fine("Completed subtask "+task);
    }
    
    synchronized (monitor)
    {
      runningTasks.remove(task);
      if (runningTasks.isEmpty())
      { monitor.notify();
      }
    }
  }  
  
  @Override
  public void taskCompleted(
    TaskEvent event)
  { subtaskCompleted(event.getSource());
  }

  @Override
  public void taskStarted(
    TaskEvent event)
  {
    
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void taskAddedResult(
    TaskEvent event,
    T result)
  { addResult((Tresult) result);
  }

  @Override
  public void taskThrewException(
    TaskEvent event,
    Exception exception)
  { addException(exception);
  }  
}
