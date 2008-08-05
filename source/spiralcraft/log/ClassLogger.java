package spiralcraft.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;



public class ClassLogger
  extends Logger
{
  
  private static ClassLogger instance;
  
  public static synchronized final ClassLogger getInstance(Class<?> subject)
  {
    if (instance==null)
    { instance=new ClassLogger();
    }
    return instance;
  }

  private Logger logger;
  private Handler logHandler=new ConsoleHandler();

  { 
    logHandler.setFormatter(new DefaultFormatter());
    logHandler.setLevel(Level.ALL);
  
//    logger=new RegistryLogger();
  }
  
  
  ClassLogger()
  { 
    super(
      "spiralcraft.log.ClassLogger"
      ,
      null
      );
    logger=Logger.getLogger(getName());
    if (logger.getHandlers()==null || logger.getHandlers().length==0)
    { logger.addHandler(logHandler);
    }
    setLevel(Level.FINEST);
  }
  
  public void levelSevere()
  { setLevel(Level.SEVERE);
  }
  
  public void levelFine()
  { setLevel(Level.FINE);
  }
  
  public void levelFiner()
  { setLevel(Level.FINER);
  }

  public void levelFinest()
  { setLevel(Level.FINEST);
  }

  @Override
  public void setLevel(Level level)
  { logger.setLevel(level);
  }

  @Override
  public void warning(String msg)
  { 
    if (isLoggable(Level.WARNING))
    { 
      StackTraceElement element
        =Thread.currentThread().getStackTrace()[2];
      
      logger.logp
        (Level.WARNING
        ,element.getClassName()
        ,element.getMethodName()+" ("+element.getFileName()+":"+element.getLineNumber()+")"
        ,msg
        );
    }
  }

  @Override
  public void fine(String msg)
  { 
    if (isLoggable(Level.FINE))
    { 
      StackTraceElement element
        =Thread.currentThread().getStackTrace()[2];
      
      logger.logp
        (Level.FINE
        ,element.getClassName()
        ,element.getMethodName()+" ("+element.getFileName()+":"+element.getLineNumber()+")"
        ,msg
        );
    }
  }

  @Override
  public void finer(String msg)
  { 
    if (isLoggable(Level.FINER))
    { 
      StackTraceElement element
        =Thread.currentThread().getStackTrace()[2];
      
      logger.logp
        (Level.FINER
        ,element.getClassName()
        ,element.getMethodName()+" ("+element.getFileName()+":"+element.getLineNumber()+")"
        ,msg
        );
    }
  }

  
  @Override
  public void finest(String msg)
  { 
    if (isLoggable(Level.FINEST))
    { 
      StackTraceElement element
        =Thread.currentThread().getStackTrace()[2];
      
      logger.logp
        (Level.FINEST
        ,element.getClassName()
        ,element.getMethodName()+" ("+element.getFileName()+":"+element.getLineNumber()+")"
        ,msg
        );
    }
  }
//  public void fine(String msg)
//  { logger.finer(msg);
//  }

//  public void fine(String msg)
//  { logger.finest(msg);
//  }

  @Override
  public void log(LogRecord event)
  { 
    System.err.println(event);
    logger.log(event);
  }
  
  @Override
  public boolean isLoggable(Level level)
  { return logger.isLoggable(level);
  }
  

  

}
