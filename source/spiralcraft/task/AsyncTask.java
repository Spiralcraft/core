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
package spiralcraft.task;

import java.util.HashSet;
import java.util.List;

/**
 * Executes a number of other Tasks in parallel
 */
public class AsyncTask
  extends AbstractTask
  implements TaskListener
{
  
  private List<? extends Task> tasks;
  private final Object monitor=new Object();

  private final HashSet<Task> runningTasks=new HashSet<Task>();
  
  private void subtaskStarting(Task task)
  { 
    synchronized (monitor)
    { runningTasks.add(task);
    }
  }
  
  private void subtaskCompleted(Task task)
  { 
    synchronized (monitor)
    {
      runningTasks.remove(task);
      if (runningTasks.isEmpty())
      { monitor.notify();
      }
    }
  }
  
  private void waitForComplete()
  { 
    synchronized (monitor)
    {
      while (!runningTasks.isEmpty())
      { 
        try
        { monitor.wait();
        }
        catch (InterruptedException x)
        {
        }
      }
    }
  }
  
  public AsyncTask(List<? extends Task> tasks)
  { this.tasks=tasks;
  }
  
  @Override
  protected void execute()
  {
    setUnitsInTask(tasks.size());
    setOpsInUnit(1);
    setUnitsCompletedInTask(0);
    setOpsCompletedInUnit(0);
    setCurrentUnitTitle("Counting");
    
    for (Task task: tasks)
    { 
      task.addTaskListener(this);
      subtaskStarting(task);
    }
    for (Task task: runningTasks)
    { task.start();
    }
    
    waitForComplete();
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
  
}
