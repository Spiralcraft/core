package spiralcraft.task;

import java.beans.PropertyChangeListener;

/**
 * A unit of work that can be managed and monitored. A Task can be composed
 *   of a number of sub-units, each of which can be composed of a number of operations.
 *
 * Tasks report on their progress and provide a means to start and stop them.
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
   * Start the task
   */
  void start();

  /**
   * Stop the task
   */
  void stop();

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
