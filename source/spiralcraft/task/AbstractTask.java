//
// Copyright (c) 1998,2005 Michael Toth
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

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import spiralcraft.util.ArrayUtil;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.time.Scheduler;

/**
 * <p>An abstract implementation of a Task which handles progress tracking,
 *   and synchronization
 * </p>
 *
 * <p>Subclasses need to implement the execute() method which is called to 
 *   perform actual task work. This abstract base class will only allow one 
 *   Thread at a time into the execute() method by virtue of a synchronized 
 *   Runnable.run() implementation.
 *
 * The execute() method should update the metrics and progress of the task.
 *
 * Stoppable tasks should check isStopRequested() at convenient points to determine whether the
 *   execute() method should continue running. If the execute() method needs to wait at some point
 *   in time, it should synchronize on the object returned by the getLock() method, which will be
 *   notified if a stop request is received. Stoppable tasks should also prepare for the Thread running
 *   the execute() method to be interrupted (to be implemented at a later date).
 * 
 * Most tasks will allow their context to supply configuration information to the task before the
 *   task is started. When this information should be prevented from being changed while the task
 *   is running, the subclass should call assertStoppedState(), which throws an IllegalStateException
 *   if the task is running.
 */
public abstract class AbstractTask
  implements Task
{
  private int _opsInUnit;
  private int _opsCompletedInUnit;
  private int _unitsInTask;
  private int _unitsCompletedInTask;
  private String _currentOpTitle;
  private String _currentUnitTitle;
  private boolean _startable=true;
  private boolean _stoppable=false;
  private boolean _running=false;
  private boolean _stopRequested=false;
  private boolean _completed=false;
  protected boolean debug;
  protected Scheduler scheduler;
  protected ClassLog log=ClassLog.getInstance(getClass());
  protected Exception exception;
  
  // private Thread _runThread;
  
  private PropertyChangeSupport _propertyChangeSupport;
  private final Object _lock=new Object();

  private TaskListener[] _taskListeners
    =new TaskListener[0];
  
  private final TaskEvent _taskEvent=new TaskEvent(this);
  
  /**
   * Perform the work specified by the task
   */
  protected abstract void work()
    throws InterruptedException;

  @Override
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public Exception getException()
  { return exception;
  }
  
  /**
   * <p>The thread Scheduler that should run the Task
   * </p>
   * @param scheduler
   */
  public void setScheduler(Scheduler scheduler)
  { this.scheduler=scheduler;
  }
  
  public int getOpsInUnit()
  { return _opsInUnit;
  }
  
  public int getOpsCompletedInUnit()
  { return _opsCompletedInUnit;
  }

  public int getUnitsInTask()
  { return _unitsInTask;
  }

  public int getUnitsCompletedInTask()
  { return _unitsCompletedInTask;
  }

  public String getCurrentOpTitle()
  { return _currentOpTitle;
  }

  public String getCurrentUnitTitle()
  { return _currentUnitTitle;
  }
  
  public boolean isStartable()
  { return _startable;
  }
  
  public boolean isStoppable()
  { return _stoppable;
  }
    
  public boolean isRunning()
  { return _running;
  }

  public boolean isStopRequested()
  { return _stopRequested;
  }
  
  public boolean isCompleted()
  { return _completed;
  }
  
  public void start()
  {
    synchronized (_lock)
    {
      if (_startable)
      {
        setStartable(false);
        if (scheduler!=null)
        { scheduler.scheduleNow(this);
        }
        else
        { Scheduler.instance().scheduleNow(this);
        }
      }
    }
  }
  
  public final synchronized void run()
  {
    synchronized (_lock)
    { 
      if (_running)
      { throw new IllegalStateException("Task is already running");
      }
      setStartable(false);
      setStoppable(true);
      setRunning(true);
      _completed=false;
      // _runThread=Thread.currentThread();
      setStopRequested(false);
      fireTaskStarted();
    }
    
    try
    { work();
    }
    catch (InterruptedException x)
    { 
      log.log(Level.WARNING,"Interrupted",x);
      addException(x);
    }
    catch (RuntimeException x)
    { 
      addException(x);
      throw x;
    }
    finally
    { 
      synchronized(_lock)
      { 
        setRunning(false);
        setStoppable(false);
        setStartable(true);
        setStopRequested(false);
        // _runThread=null;
        _completed=true;
        fireTaskCompleted();
      }
    }
  }
  
  public void stop()
  {
    synchronized (_lock)
    {
      if (!_completed)
      {
        if (_stoppable && !_stopRequested)
        { setStopRequested(true);
        }
        _lock.notifyAll();
      }
    }
    
  }

  protected Object getLock()
  { return _lock;
  }
  
  protected void setOpsCompletedInUnit(int val)
  { 
    firePropertyChange
      ("opsCompletedInUnit"
      ,new Integer(_opsCompletedInUnit)
      ,new Integer(_opsCompletedInUnit=val)
      );
  }

  protected void setUnitsCompletedInTask(int val)
  { 
    firePropertyChange
      ("unitsCompletedInTask"
      ,new Integer(_unitsCompletedInTask)
      ,new Integer(_unitsCompletedInTask=val)
      );
  }

  protected void setUnitsInTask(int val)
  { 
    firePropertyChange
      ("unitsInTask"
      ,new Integer(_unitsInTask)
      ,new Integer(_unitsInTask=val)
      );
  }

  protected void setOpsInUnit(int val)
  { 
    firePropertyChange
      ("opsInUnit"
      ,new Integer(_opsInUnit)
      ,new Integer(_opsInUnit=val)
      );
  }

  protected void setCurrentOpTitle(String val)
  { 
    firePropertyChange
      ("currentOpTitle"
      ,_currentOpTitle
      ,_currentOpTitle=val
      );
  }

  protected void setCurrentUnitTitle(String val)
  { 
    firePropertyChange
      ("currentUnitTitle"
      ,_currentUnitTitle
      ,_currentUnitTitle=val
      );
  }

  protected void setStartable(boolean val)
  {
    firePropertyChange
      ("startable"
      ,_startable?Boolean.TRUE:Boolean.FALSE
      ,(_startable=val)?Boolean.TRUE:Boolean.FALSE
      );
  }

  protected void setStoppable(boolean val)
  {
    firePropertyChange
      ("stoppable"
      ,_stoppable?Boolean.TRUE:Boolean.FALSE
      ,(_stoppable=val)?Boolean.TRUE:Boolean.FALSE
      );
  }
  
  protected void setRunning(boolean val)
  {
    firePropertyChange
      ("running"
      ,_running?Boolean.TRUE:Boolean.FALSE
      ,(_running=val)?Boolean.TRUE:Boolean.FALSE
      );
  }

  protected void setStopRequested(boolean val)
  {
    firePropertyChange
      ("stopRequested"
      ,_stopRequested?Boolean.TRUE:Boolean.FALSE
      ,(_stopRequested=val)?Boolean.TRUE:Boolean.FALSE
      );
  }

  /**
   * Throw an IllegalStateException if the task is running.
   */
  protected void assertStoppedState()
  {
    synchronized (_lock)
    {
      if (_running)
      { throw new IllegalStateException("Task is running");
      }
    }
  }
  
  /**
   * Add a listener to be notified when enabled state changes.
   */
  public void addPropertyChangeListener
    (PropertyChangeListener listener)
  {
    if (_propertyChangeSupport==null)
    { _propertyChangeSupport=new PropertyChangeSupport(this);
    }
    _propertyChangeSupport.addPropertyChangeListener(listener);    
  }

  public void removePropertyChangeListener
    (PropertyChangeListener listener)
  {
    if (_propertyChangeSupport!=null)
    {
      _propertyChangeSupport
        .removePropertyChangeListener(listener);
    }
  }

  protected void firePropertyChange(String name,Object oldVal,Object newVal)
  {
    if (_propertyChangeSupport!=null)
    { _propertyChangeSupport.firePropertyChange(name,oldVal,newVal);
    }
  }
  
  /**
   * One or more subcommands completed.
   * 
   * @param result
   */
  protected void addResult(Object result)
  {
    for (int i=0;i<_taskListeners.length;i++)
    { 
      _taskListeners[i]
        .taskAddedResult(_taskEvent,result);
    }    
  }
  
  protected void addException(Exception exception)
  { 
    this.exception=exception;
    for (int i=0;i<_taskListeners.length;i++)
    { _taskListeners[i].taskThrewException(_taskEvent,exception);
    }    
  }

  
  public void addTaskListener(TaskListener listener)
  { 
    _taskListeners
      =(TaskListener[]) ArrayUtil.append(_taskListeners,listener);
  }
  
  public void removeTaskListener(TaskListener listener)
  { 
    _taskListeners
      =(TaskListener[]) ArrayUtil.remove(_taskListeners,listener);
  }

  protected TaskCommand executeChild(Scenario scenario)
  { 
    TaskCommand command=scenario.command();
    command.execute();
    if (command.getException()!=null)
    { addException(command.getException());
    }
    return command;
  }
  
  private void fireTaskStarted()
  { 
    for (int i=0;i<_taskListeners.length;i++)
    { _taskListeners[i].taskStarted(_taskEvent);
    }
  }
  
  private void fireTaskCompleted()
  {
    for (int i=0;i<_taskListeners.length;i++)
    { _taskListeners[i].taskCompleted(_taskEvent);
    }
  }
  
}
