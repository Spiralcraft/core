package spiralcraft.io.test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import spiralcraft.common.LifecycleException;
import spiralcraft.io.OutputAgent;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.ParallelTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;

public class OutputAgentTest
  extends Scenario
{
  
  private OutputAgent agent;
  private AtomicInteger seq=new AtomicInteger(0);
  private int entryLimit=1;
  private int delay;
  private int threadCount=1;
  
  public void setAgent(OutputAgent agent)
  { this.agent=agent;
  }

  public void setEntryLimit(int entryLimit)
  { this.entryLimit=entryLimit;
  }
  
  public void setDelayMS(int delay)
  { this.delay=delay;
  }
  
  public void setThreadCount(int threadCount)
  { this.threadCount=threadCount;
  }
  
  
  @Override
  protected Task task()
  {
    ArrayList<Task> list=new ArrayList<Task>();
    for (int i=0;i<threadCount;i++)
    { 
      final int threadNum=i;
      list.add(new AbstractTask()
        {
            
          @Override
          public void work()
          { 
            try
            {
              while (!this.isStopRequested() && seq.intValue()<entryLimit)
              { 
                int useSeq=seq.incrementAndGet();
                long time=System.currentTimeMillis();
                agent.write
                  ((threadNum+"-#"+(useSeq)+": @"+time+": This is a test of the OutputAgent \r\n")
                    .getBytes()
                  );
                long waited=System.currentTimeMillis()-time;
                if (waited>250)
                { 
                  log.log
                    (Level.WARNING
                     ,"OutputAgent.write call "+threadNum
                     +"-#"+useSeq+" took "+waited+" ms"
                     );
                }
                if (delay>0)
                { Thread.sleep(delay);
                }   
                
              }

            }
            catch (Exception x)
            { log.log(Level.SEVERE,"Error",x);
            }
          }
        });
    }
    return new ParallelTask<Task>(list)
    {
      @Override
      public void work()
      {
        super.work();
        if (chain!=null && exception!=null)
        { addResult(executeChild(chain));
        }
      }
    };
    
  }

  @Override
  public void start()
    throws LifecycleException
  { agent.start();
  }
  
  @Override
  public void stop()
    throws LifecycleException
  { agent.stop();
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
  { return focusChain.chain(new SimpleChannel<OutputAgent>(agent,true));
  }
  
    
}
