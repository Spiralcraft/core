package spiralcraft.task.test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.task.TaskCommand;
import spiralcraft.test.Test;
import spiralcraft.test.TestFailedException;
import spiralcraft.test.TestResult;
import spiralcraft.util.refpool.URIPool;

/**
 * <p>Runs a Scenario and stops it while it runs, then tests what happened
 *   against a StopReport, as an Assertion tests its subject.
 * </p>
 *
 * <p>The WaitScenarios and MarkScenarios in the subject register with the
 *   StopRun that encloses them when they are bound.
 * </p>
 *
 * <p>How the subject is stopped:
 * </p>
 * <ul>
 *   <li>command: the subject runs in its own thread and, once every
 *     WaitScenario has started, TaskCommand.stop() is called
 *   <li>lifecycle: as command, but the subject's Scenario.stop() is called,
 *     then the gates are opened
 *   <li>before: TaskCommand.stop() is called before the command is executed
 *   <li>lifecycle-before: the subject's Scenario.stop() is called before the
 *     command is executed
 *   <li>none: the subject runs to its end in this thread, so only what the
 *     subject does to itself stops it
 *   <li>twice: as command, then a new command of the same subject is
 *     executed to its end
 * </ul>
 */
public class StopRun
  extends Test
{
  public static StopRun find(Focus<?> focus)
  {
    Focus<StopRun> runFocus
      =focus.<StopRun>findFocus
        (URIPool.create("class:/spiralcraft/task/test/StopRun"));
    if (runFocus==null)
    { return null;
    }
    return runFocus.getSubject().get();
  }

  private static final long JOIN_MS=10000;

  private Scenario<?,?> subject;
  private String how="command";
  private Expression<Boolean> testX;
  private Channel<Boolean> testChannel;
  private ThreadLocalChannel<StopReport> reportChannel;
  private final Map<String,WaitScenario> waiters
    =new LinkedHashMap<String,WaitScenario>();
  private final Map<String,MarkScenario> markers
    =new LinkedHashMap<String,MarkScenario>();
  private final Map<String,ProbeScenario> probes
    =new LinkedHashMap<String,ProbeScenario>();
  private final AtomicInteger touches=new AtomicInteger();
  private volatile TaskCommand<?,?> current;

  public void setSubject(Scenario<?,?> subject)
  { this.subject=subject;
  }

  public void setHow(String how)
  { this.how=how;
  }

  public void setTestX(Expression<Boolean> testX)
  { this.testX=testX;
  }

  /**
   * Count a call from an expression in the subject, such as a resultX that
   *   should not be evaluated when the subject stops
   */
  public int touch()
  { return touches.incrementAndGet();
  }

  /**
   * Stop the command being run by this StopRun, from an expression in the
   *   subject: used where the stop has to arrive at a particular point
   */
  public boolean stopNow()
  {
    current.stop();
    return true;
  }

  void register(WaitScenario waiter)
  { waiters.put(waiter.getName(),waiter);
  }

  void register(MarkScenario marker)
  { markers.put(marker.getName(),marker);
  }

  void register(ProbeScenario probe)
  { probes.put(probe.getName(),probe);
  }

  /**
   * Whether the subject is stopping now
   */
  boolean subjectStopping()
  { return subject.isStopping();
  }

  /**
   * The public result of a run, for the test expression
   */
  public class StopReport
  {
    private TaskCommand<?,?> command;
    private TaskCommand<?,?> secondCommand;

    public AbstractTask getTask()
    { return (AbstractTask) command.getTask();
    }

    public Exception getException()
    { return command.getException();
    }

    public AbstractTask getSecondTask()
    {
      return secondCommand!=null
        ?(AbstractTask) secondCommand.getTask()
        :null;
    }

    public Exception getSecondException()
    {
      return secondCommand!=null
        ?secondCommand.getException()
        :null;
    }

    public int getTouches()
    { return touches.get();
    }

    public WaitScenario waiter(String name)
    { return waiters.get(name);
    }

    public MarkScenario marker(String name)
    { return markers.get(name);
    }

    public ProbeScenario probe(String name)
    { return probes.get(name);
    }

    /**
     * The subject's stopping flag
     */
    public boolean isSubjectStopping()
    { return subject.isStopping();
    }

    /**
     * Call stopRequested() on the subject twice, then report its flag
     */
    public boolean requestTwice()
    {
      subject.stopRequested();
      subject.stopRequested();
      return subject.isStopping();
    }

    /**
     * Stop the subject, start it again, then report its flag
     */
    public boolean stopThenStart()
    {
      try
      { 
        subject.stop();
        subject.start();
      }
      catch (LifecycleException x)
      { throw new RuntimeException(x);
      }
      return subject.isStopping();
    }

    @Override
    public String toString()
    {
      StringBuilder out=new StringBuilder();
      out.append("how=").append(how);
      out.append(" task=");
      describe(out,(AbstractTask) command.getTask());
      out.append(" exception=").append(command.getException());
      out.append(" touches=").append(touches.get());
      if (secondCommand!=null)
      {
        out.append(" second=");
        describe(out,(AbstractTask) secondCommand.getTask());
        out.append(" secondException=").append(secondCommand.getException());
      }
      for (WaitScenario waiter: waiters.values())
      {
        out.append(" wait ").append(waiter.getName())
          .append(": starts=").append(waiter.getStarts())
          .append(" created=").append(waiter.getCreated())
          .append(" ends=").append(waiter.getEnds())
          .append(" timedOut=").append(waiter.isTimedOut());
        if (waiter.getLastTask()!=null)
        {
          out.append(" ");
          describe(out,waiter.getLastTask());
        }
      }
      for (MarkScenario marker: markers.values())
      {
        out.append(" mark ").append(marker.getName())
          .append(": created=").append(marker.getCreated())
          .append(" runs=").append(marker.getRuns());
      }
      return out.toString();
    }

    private void describe(StringBuilder out,AbstractTask task)
    {
      out.append("[completed=").append(task.isCompleted())
        .append(" stopRequested=").append(task.isStopRequested())
        .append(" stopped=").append(task.isStopped())
        .append("]");
    }
  }

  @Override
  protected Task task()
  {
    return new AbstractTask()
    {
      @Override
      protected void work()
        throws InterruptedException
      {
        StopReport report=new StopReport();
        StopRun.this.run(report);

        TestResult result;
        reportChannel.push(report);
        try
        {
          Boolean condition=testChannel.get();
          result
            =new TestResult
              (name
              ,Boolean.TRUE.equals(condition)
              ,"Report: "+report+" , testing ["+testX.getText()+"]"
              );
        }
        catch (RuntimeException x)
        { result=new TestResult(name,false,"Caught Exception",x);
        }
        finally
        { reportChannel.pop();
        }

        if (testGroup!=null)
        { testGroup.addTestResult(result);
        }
        if (throwFailure && !result.getPassed())
        { addException(new TestFailedException(result,result.getException()));
        }
        addResult(result);
      }
    };
  }

  private void run(final StopReport report)
    throws InterruptedException
  {
    final TaskCommand<?,?> command=subject.command();
    report.command=command;
    current=command;

    if (how.equals("before"))
    {
      command.stop();
      finish(execute(command));
    }
    else if (how.equals("lifecycle-before"))
    {
      stopSubject();
      finish(execute(command));
    }
    else if (how.equals("none"))
    { finish(execute(command));
    }
    else
    {
      Thread thread=execute(command);
      for (WaitScenario waiter: waiters.values())
      {
        if (!waiter.awaitStarted(WaitScenario.WAIT_MS))
        { log.log(Level.WARNING,name+": "+waiter.getName()+" did not start");
        }
      }

      if (how.equals("lifecycle"))
      {
        stopSubject();
        for (WaitScenario waiter: waiters.values())
        { waiter.openGate();
        }
      }
      else
      { command.stop();
      }
      finish(thread);

      if (how.equals("twice"))
      {
        report.secondCommand=subject.command();
        finish(execute(report.secondCommand));
      }
    }
  }

  private void stopSubject()
  {
    try
    { subject.stop();
    }
    catch (LifecycleException x)
    { throw new RuntimeException(x);
    }
  }

  /**
   * Execute the command in its own thread, so that a subject which does not
   *   stop fails the test and does not hang it
   */
  private Thread execute(final TaskCommand<?,?> command)
  {
    Thread thread
      =new Thread("StopRun "+name)
      {
        @Override
        public void run()
        { command.execute();
        }
      };
    thread.setDaemon(true);
    thread.start();
    return thread;
  }

  private void finish(Thread thread)
    throws InterruptedException
  {
    thread.join(JOIN_MS);
    if (thread.isAlive())
    { log.log(Level.WARNING,name+": subject still running after "+JOIN_MS+"ms");
    }
  }

  @Override
  public void start()
    throws LifecycleException
  {
    super.start();
    if (subject!=null)
    { subject.start();
    }
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    if (subject!=null)
    { subject.stop();
    }
    super.stop();
  }

  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws ContextualException
  {
    subject.bind(focusChain);

    reportChannel
      =new ThreadLocalChannel<StopReport>
        (BeanReflector.<StopReport>getInstance(StopReport.class));
    testChannel
      =focusChain.<StopReport>chain(reportChannel).<Boolean>bind(testX);
    super.bindChildren(focusChain);
  }

}
