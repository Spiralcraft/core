package spiralcraft.service;

import java.util.ArrayList;

import spiralcraft.app.kit.AbstractComponent;

/**
 * Asynchronously launches long running external processes and tracks their state
 *
 * @author mike
 *
 */
public class ProcessService
  extends AbstractComponent
  implements Service
{
  private ArrayList<ProcessTracker> processes=new ArrayList<>();
  
  public ProcessTracker launch(ProcessBuilder builder)
  { 
    ProcessTracker ret=new ProcessTracker(builder,this);
    this.processes.add(ret);
    ret.start();
    if (ret.getProcess()==null)
    { this.processes.remove(ret);
    }
    else
    { log.fine("Started pid "+ret.getProcess().pid());
    }
    return ret;
  }
  
  void remove(ProcessTracker tracker)
  { 
    log.fine("Pid "+tracker.getProcess().pid()+" completed");
    processes.remove(tracker);
  }

}