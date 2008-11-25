package spiralcraft.log;

import spiralcraft.util.thread.ThreadLocalStack;

/**
 * <p>An implementation of the Log interface which uses the ThreadContext to
 *   locate the appropriate Log to delegate to.
 * </p>
 * 
 * @author mike
 *
 */
public class ContextLog
  implements Log
{
  private static final GenericLog DEFAULT_LOG
    =new GenericLog(null);
  static
  {
    DEFAULT_LOG.setHandlers
      (new ConsoleHandler());
  }

  
  private static ThreadLocalStack<Log> stack
    =new ThreadLocalStack<Log>()
    {
       @Override
       public Log defaultValue()
       { return DEFAULT_LOG;
       };
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
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void log(
    Level level,
    String message,
    Throwable thrown)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void log(
    Event event)
  { getInstance().log(event);
  }

}
