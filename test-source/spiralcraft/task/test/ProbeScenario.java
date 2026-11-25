package spiralcraft.task.test;

import java.util.concurrent.atomic.AtomicInteger;

import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.Focus;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;

/**
 * <p>A test Scenario that records whether the subject of the StopRun it
 *   registers with was already stopping when its own stop() was called, to
 *   show that a composite sets its flag before it stops its children.
 * </p>
 */
public class ProbeScenario
  extends Scenario<Void,Void>
{
  private String name;
  private StopRun run;
  private final AtomicInteger stops=new AtomicInteger();
  private final AtomicInteger runs=new AtomicInteger();
  private volatile boolean stoppingAtStop;

  public void setName(String name)
  { this.name=name;
  }

  public String getName()
  { return name;
  }

  /**
   * The number of times stop() was called
   */
  public int getStops()
  { return stops.get();
  }

  public int getRuns()
  { return runs.get();
  }

  /**
   * Whether the StopRun subject's isStopping() was true when stop() was
   *   called on this Scenario (the last time)
   */
  public boolean isStoppingAtStop()
  { return stoppingAtStop;
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  {
    run=StopRun.find(focusChain);
    if (run!=null)
    { run.register(this);
    }
    return super.bind(focusChain);
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    stoppingAtStop=run!=null && run.subjectStopping();
    stops.incrementAndGet();
    super.stop();
  }

  @Override
  protected Task task()
  {
    return new AbstractTask()
    {
      @Override
      protected void work()
      { runs.incrementAndGet();
      }
    };
  }
}
