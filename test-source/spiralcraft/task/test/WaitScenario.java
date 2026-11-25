package spiralcraft.task.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.task.TaskCommand;

/**
 * <p>A test Scenario whose task waits until it is stopped, or until a gate
 *   is opened, and records what happened to it. Used with a StopRun, which
 *   it registers with when it is bound.
 * </p>
 *
 * <p>A wait that nothing ends gives up after WAIT_MS and records timedOut, so
 *   a test that fails does not hang.
 * </p>
 */
public class WaitScenario
  extends Scenario<Void,Void>
{
  public static final long WAIT_MS=5000;

  private String name;
  private boolean selfStop;
  private boolean gated;
  private int waits=Integer.MAX_VALUE;

  private final CountDownLatch started=new CountDownLatch(1);
  private final CountDownLatch gate=new CountDownLatch(1);
  private final AtomicInteger starts=new AtomicInteger();
  private final AtomicInteger ends=new AtomicInteger();
  private final AtomicInteger created=new AtomicInteger();
  private volatile boolean timedOut;
  private volatile AbstractTask lastTask;

  public void setName(String name)
  { this.name=name;
  }

  public String getName()
  { return name;
  }

  /**
   * The task stops itself, through the TaskCommand that the Scenario
   *   publishes for the thread running it, once it has started
   */
  public void setSelfStop(boolean selfStop)
  { this.selfStop=selfStop;
  }

  /**
   * Wait for the gate to be opened, ignoring any stop, instead of waiting
   *   for a stop
   */
  public void setGated(boolean gated)
  { this.gated=gated;
  }

  /**
   * The number of runs which wait; later runs end at once. Defaults to all.
   */
  public void setWaits(int waits)
  { this.waits=waits;
  }

  public boolean awaitStarted(long ms)
    throws InterruptedException
  { return started.await(ms,TimeUnit.MILLISECONDS);
  }

  public void openGate()
  { gate.countDown();
  }

  public int getStarts()
  { return starts.get();
  }

  public int getEnds()
  { return ends.get();
  }

  /**
   * The number of tasks created, one for each command made
   */
  public int getCreated()
  { return created.get();
  }

  public boolean isTimedOut()
  { return timedOut;
  }

  /**
   * The task of the last command created by this Scenario
   */
  public AbstractTask getLastTask()
  { return lastTask;
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
    AbstractTask task
      =new AbstractTask()
    {
      @Override
      protected void work()
        throws InterruptedException
      {
        int run=starts.incrementAndGet();
        started.countDown();
        try
        {
          if (selfStop)
          {
            TaskCommand<?,?> own=commandChannel.get();
            own.stop();
          }

          if (run<=waits)
          {
            if (gated)
            {
              if (!gate.await(WAIT_MS,TimeUnit.MILLISECONDS))
              { timedOut=true;
              }
            }
            else
            {
              long deadline=System.currentTimeMillis()+WAIT_MS;
              synchronized (getLock())
              {
                while (!isStopRequested())
                {
                  long left=deadline-System.currentTimeMillis();
                  if (left<=0)
                  {
                    timedOut=true;
                    break;
                  }
                  getLock().wait(left);
                }
              }
            }
          }
        }
        finally
        { ends.incrementAndGet();
        }
      }
    };
    lastTask=task;
    return task;
  }

}
