package spiralcraft.task.test;

import java.util.concurrent.atomic.AtomicInteger;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;

/**
 * <p>A test Scenario whose task only counts its runs, to show whether a
 *   task that should have been skipped ran. Used with a StopRun, which it
 *   registers with when it is bound.
 * </p>
 */
public class MarkScenario
  extends Scenario<Void,Void>
{
  private String name;
  private final AtomicInteger runs=new AtomicInteger();
  private final AtomicInteger created=new AtomicInteger();

  public void setName(String name)
  { this.name=name;
  }

  public String getName()
  { return name;
  }

  public int getRuns()
  { return runs.get();
  }

  /**
   * The number of tasks created, one for each command made
   */
  public int getCreated()
  { return created.get();
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  {
    StopRun run=StopRun.find(focusChain);
    if (run!=null)
    { run.register(this);
    }
    return super.bind(focusChain);
  }

  @Override
  protected Task task()
  {
    created.incrementAndGet();
    return new AbstractTask()
    {
      @Override
      protected void work()
      { runs.incrementAndGet();
      }
    };
  }

}
