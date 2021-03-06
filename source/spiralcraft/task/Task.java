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
 * <p>A unit of work that can be managed and monitored. A Task instance 
 *   represents the state and progress of the invocation of some operation,
 *   and thus has a life-cycle scoped to that invocation.
 * </p>
 * 
 * <p>A Task can be composed of a number of "sub-units", each of which can be 
 *   composed of a number of operations.
 * </p>
 *
 * <p>Tasks report on their progress and provide a means to asynchronously
 *   start and stop them, or to run them synchronously.
 * </p>
 * 
 * <p>A Task is generally created and configured by a Scenario.
 * </p>
 * 
 * <p>A Task may generate results at any time during its running. The results
 *   are sent to the TaskListeners registered with the Task.
 * </p>
 */
public interface Task
  extends Runnable
{
  
  /**
   * The total number of operations in the currently executing work unit
   */
  int getOpsInUnit();
  
  /**
   * The number of operations completed so far in the currently executing
   *   work unit.
   */
  int getOpsCompletedInUnit();
  
  /**
   * The number of work units in the task
   */
  int getUnitsInTask();

  /**
   * The number of work units completed in the task
   */
  int getUnitsCompletedInTask();

  /**
   * The title of the currently executing operation
   */
  String getCurrentOpTitle();

  /**
   * The title of the currently executing work unit
   */
  String getCurrentUnitTitle();
  
  /**
   * Whether the task can be started at the current time
   */
  boolean isStartable();

  /**
   * Whether the task can be stopped at the current time
   */
  boolean isStoppable();

  /**
   * Whether the task is currently running
   */
  boolean isRunning();
  
  /**
   * Whether the task has completed
   * 
   * @return
   */
  boolean isCompleted();
  
  /**
   * Activate debug logging
   */
  void setDebug(boolean debug);
  
  /**
   * <p>The Scheduler context that should run the task
   * </p>
   * @param scheduler
   */
  void setScheduler(Scheduler scheduler);
  
  /**
   * @return The Exception, if any, that caused the task to prematurely
   *   terminate
   */
  Exception getException();
  
  /**
   * Start the task asynchronously in a new thread
   */
  void start();

  /**
   * Signal that the task should stop
   */
  void stop();
  
  /**
   * Run the task in the current thread and return when complete
   */
  @Override
  void run();

  /**
   * Register a listener to be notified of property changes
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Deregister a listener to be notified of property changes
   */
  void removePropertyChangeListener(PropertyChangeListener listener);
  
  /**
   * Register a TaskListener to be informed of stop/start events
   */
  void addTaskListener(TaskListener listener);

  /**
   * Deregister a TaskListener to be informed of stop/start events
   */
  void removeTaskListener(TaskListener listener);
}
