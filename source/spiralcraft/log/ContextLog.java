package spiralcraft.log;

import spiralcraft.util.Path;
import spiralcraft.util.thread.ThreadLocalStack;

/**
 * <p>An implementation of the Log interface which delegates to the 
 *   Thread context log. 
 * </p>
 * 
 * @author mike
 *
 */
public class ContextLog
  implements Log
{
  private static GenericLog DEFAULT_LOG
    =GlobalLog.instance();
  
  protected Path context;
   
  
  private static ThreadLocalStack<Log> stack
    =new ThreadLocalStack<Log>(true)
    {
       @Override
       public Log defaultValue()
       { return DEFAULT_LOG;
       }
    };
  
  public static void push(Log log)
  { stack.push(log);
  }
  
  public static void pop()
  { stack.pop();
  }
  
  public static Log getInstance()
  { return stack.get();
  }
  
  @Override
  public boolean canLog(Level level)
  { return getInstance().canLog(level);
  }

  @Override
  public void log(
    Level level,
    String message)
  { log(level,message,null,1);
  }

  @Override
  public void log(
    Level level,
    String message,
    Throwable thrown)
  { 
    log(level,message,thrown,1);
  }

  @Override
  public void log(
    Event event)
  { getInstance().log(event);
  }
  

  protected void log(
    Level level,
    String message,
    Throwable thrown
    ,int traceDepth
    )
  {
    Log log=getInstance();
    if (log.canLog(level))
    { 
      log.log
        (new Event
          (Thread.currentThread().getStackTrace()[2+traceDepth]
          ,context
          ,level
          ,message
          ,thrown
          ,null
          )
        );
    }
    
  }

}
