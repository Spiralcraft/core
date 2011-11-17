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
public class TaskCommand<Tcontext,Tresult>
  extends CommandAdapter<Task,Tcontext,Tresult>
  implements TaskListener
{
  
  protected final Task task;
  protected boolean collectResults=false;
  protected final Scenario<Tcontext,Tresult> scenario;
  protected ClosureFocus<?>.Closure closure;
  protected Object error;
  
  public TaskCommand
    (Scenario<Tcontext,Tresult> scenario,Task task,Tcontext initContext)
  { 
    this.task=task;
    setTarget(task);
    this.task.addTaskListener(this);
    this.scenario=scenario;
    if (initContext!=null)
    { setContext(initContext);
    }

    
  }
  
  public Task getTask()
  { return task;
  }
  
  public void setError(Object error)
  { this.error=error;
  }
  
  public Object getError()
  { return error;
  }
  
  @Override
  public void run()
  { 
    if (isCompleted())
    { 
      throw new IllegalStateException
        ("Cannot invoke a Command instance more than once");
    }
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
        if (scenario.getWhenX()==null 
            || Boolean.TRUE.equals(scenario.getWhenX().get())
           )
        {
          task.run();
          setException(task.getException());
          onTaskCompletion();
        }
        else
        { 
          if (scenario.debug)
          { 
            scenario.log.fine
              ("Scenario skipped b/c whenX returned non-TRUE: "
              +scenario.getWhenX().getText()
              );
          }
        }
      }
      if (error!=null)
      { 
        setException
          (new Exception
            ("Task completed in error state: error="+error.toString()));
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
      notifyCompleted();
    }
  }

  /** 
   * Override to run something when the task associated with this
   *   command is completed (ie. to publish results, etc).
   */
  protected void onTaskCompletion()
  {
  }
  
  @Override
  public void setResult(Tresult result)
  { 
    if (isCompleted())
    { 
      throw new IllegalStateException
        ("Cannot change the result of a completed command");
    }
    super.setResult(result);
  }
  
  /**
   * 
   */
  public void encloseContext()
  { 
    if (isCompleted())
    { 
      throw new IllegalStateException
        ("Cannot change the closure state of a completed command");
    }
    if (closure!=null)
    { throw new IllegalStateException("Context already enclosed");
    }
    scenario.pushCommand(this);
    try
    { closure=scenario.enclose();
    }
    finally
    { scenario.popCommand();
    }
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
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
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
      { setResult((Tresult) new ArrayList());
      }
      
      if (getResult() instanceof List)
      {
        // XXX Use Decorator on result channel
        ((List) getResult()).add(result);
      }
      else
      { 
        throw new IllegalArgumentException
          ("Cannot collect results- existing result has unexpected type "
          +getResult()+". Scenario: "+getTarget()
          );
      }
    }
    else if (scenario.getStoreResults())
    { 
      if (getResult()==null)
      { setResult((Tresult) result);
      }
      else
      { 
        scenario.log.warning
          ("Replacing previous task result "+getResult()+" with "+result);
      }
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
