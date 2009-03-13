package spiralcraft.task;

import spiralcraft.command.Command;
import spiralcraft.common.LifecycleException;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.log.Log;

public class TaskRunner
  implements Executable
{

  private Scenario<? extends Task,?> scenario;
  
  private final Log log=ClassLog.getInstance(TaskRunner.class);
  
  
  public void setScenario(Scenario<? extends Task,?> scenario)
  { this.scenario=scenario;
  }
  
  @Override
  public void execute(
    String... args)
    throws ExecutionException
  {
    try
    {
      scenario.bind
        (new SimpleFocus<TaskRunner>
          (new SimpleChannel<TaskRunner>(this,true)
          )
        );

      scenario.start();
      try
      { 
        Command<?,?> command=scenario.command();
        command.execute();
        if (command.getResult()!=null)
        { 
          log.log
            (Level.INFO,"Scenario "+scenario+" completed with result: "
            +command.getResult()
            );
        }
        if (command.getException()!=null)
        {
          log.log
            (Level.SEVERE,"Scenario "+scenario+" complete with exception."
            ,command.getException()
            );
        }
      }
      catch (Throwable x)
      { log.log(Level.SEVERE,"Uncaught exception running "+scenario,x);
      }
      scenario.stop();
    }
    catch (BindException x)
    { throw new ExecutionException("Error binding focus",x);
    }    
    catch (LifecycleException x)
    { throw new ExecutionException("Error starting/stopping scenario",x);
    }    
  }

}
