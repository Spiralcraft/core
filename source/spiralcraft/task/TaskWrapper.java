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

import java.beans.PropertyChangeListener;

import spiralcraft.time.Scheduler;

/**
 * Delegates to another task
 * 
 * @author mike
 *
 */
public class TaskWrapper
  implements Task
{
  private final Task task;

  public TaskWrapper(Task task)
  { this.task=task;
  }

  @Override
  public void addPropertyChangeListener(
    PropertyChangeListener listener)
  { task.addPropertyChangeListener(listener);
  }

  @Override
  public void addTaskListener(
    TaskListener listener)
  { task.addTaskListener(listener);
  }

  @Override
  public String getCurrentOpTitle()
  { return task.getCurrentOpTitle();
  }

  @Override
  public String getCurrentUnitTitle()
  { return task.getCurrentUnitTitle();
  }

  @Override
  public Exception getException()
  { return task.getException();
  }

  @Override
  public int getOpsCompletedInUnit()
  { return task.getOpsCompletedInUnit();
  }

  @Override
  public int getOpsInUnit()
  { return task.getOpsInUnit();
  }

  @Override
  public int getUnitsCompletedInTask()
  { return task.getUnitsCompletedInTask();
  }

  @Override
  public int getUnitsInTask()
  { return task.getUnitsInTask();
  }

  @Override
  public boolean isCompleted()
  { return task.isCompleted();
  }

  @Override
  public boolean isRunning()
  { return task.isRunning();
  }

  @Override
  public boolean isStartable()
  { return task.isStartable();
  }

  @Override
  public boolean isStoppable()
  { return task.isStoppable();
  }

  @Override
  public void removePropertyChangeListener(
    PropertyChangeListener listener)
  { task.removePropertyChangeListener(listener);
  }

  @Override
  public void removeTaskListener(
    TaskListener listener)
  { task.removeTaskListener(listener);
  }

  @Override
  public void run()
  { task.run();
  }

  @Override
  public void setDebug(
    boolean debug)
  { task.setDebug(debug);
  }

  @Override
  public void setScheduler(
    Scheduler scheduler)
  { task.setScheduler(scheduler);
  }

  @Override
  public void start()
  { task.start();
  }

  @Override
  public void stop()
  { task.stop();
  }
}
