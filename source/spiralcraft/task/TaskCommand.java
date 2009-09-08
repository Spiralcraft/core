//
// Copyright (c) 2009,2009 Michael Toth
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

import java.util.ArrayList;
import java.util.List;

import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.lang.spi.ClosureFocus;


/**
 * <p>A Command that encapsulates the execution of a specific type of Task.
 * </p>
 * 
 * <p>The Command can optionally accumulate Task results in an application
 *   specific manner.
 * </p>
 * @author mike
 *
 */
public class TaskCommand
  extends CommandAdapter<Task,List<?>>
  implements TaskListener
{
  
  protected final Task task;
  protected boolean collectResults=false;
  protected final Scenario scenario;
  protected ClosureFocus<?>.Closure closure;
  
  public TaskCommand(Scenario scenario,Task task)
  { 
    this.task=task;
    setTarget(task);
    this.task.addTaskListener(this);
    this.scenario=scenario;
  }
  
  public Task getTask()
  { return task;
  }
  
  @Override
  public void run()
  { 
    scenario.pushCommand(this);
    if (closure!=null)
    { closure.push();
    }
    try
    { 
      if (task==null)
      { throw new IllegalStateException("No task specified");
      }
      else
      { 
        task.run();
        setException(task.getException());
        onTaskCompletion();
      }
    }
    catch (RuntimeException x)
    { 
      setException(x);
      throw x;
    }
    finally
    { 
      if (closure!=null)
      { closure.pop();
      }
      scenario.popCommand();
    }
  }

  /** 
   * Override to run something when the task associated with this
   *   command is completed (ie. to publish results, etc).
   */
  protected void onTaskCompletion()
  {
  }
  
  /**
   * 
   */
  public void encloseContext()
  { 
    if (closure!=null)
    { throw new IllegalStateException("Context already enclosed");
    }
    closure=scenario.enclose();
  }
  
  /**
   * Indicate whether the command should collect the results generated by
   *   the running task. Defaults to false. Set to true in situations
   *   where Command.getResult() will be used instead of adding a 
   *   TaskListener to the contained task.
   * 
   * @param collectResults
   */
  public void setCollectResults(boolean collectResults)
  { this.collectResults=collectResults;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void taskAddedResult(
    TaskEvent event,
    Object result)
  { 
    if (scenario.getLogTaskResults())
    { scenario.logTaskResult(event,result);
    }
    
    if (collectResults)
    {
      if (getResult()==null)
      { setResult(new ArrayList<Command<?,?>>());
      }
      ((List) getResult()).add(result);
    }
  }

  @Override
  public void taskCompleted(
    TaskEvent event)
  { 
    
  }

  @Override
  public void taskStarted(
    TaskEvent event)
  {
    
  }

  @Override
  public void taskThrewException(
    TaskEvent event,
    Exception exception)
  { 
    if (scenario.getLogTaskResults())
    { scenario.logTaskException(event,exception);
    }
    
  }
  
}
