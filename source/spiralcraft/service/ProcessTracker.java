package spiralcraft.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ProcessTracker
{
  private ProcessBuilder builder;
  private IOException launchException;
  private Process process;
  private ProcessService service;
  private CompletableFuture<Process> onExit;
  
  
  public ProcessTracker(ProcessBuilder builder,ProcessService service)
  { 
    this.service=service;
    this.builder=builder;
  }
  
  
  public void start()
  {
    try
    { 
      this.process=builder.start();
      onExit=process.onExit();
      onExit.thenAcceptAsync(p -> cleanup());
    }
    catch (IOException x)
    { launchException=x;
    }
  }
  
  public IOException getLaunchException()
  { return this.launchException;
  }
  
  public Process getProcess()
  { return this.process;
  }
  
  public void destroy()
  { process.destroy();
  }
  
  public void cleanup()
  { service.remove(this);
  }
  
}