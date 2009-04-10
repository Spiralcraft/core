package spiralcraft.log;

import java.io.File;
import java.io.IOException;

import spiralcraft.common.LifecycleException;
import spiralcraft.io.RotatingFileOutputAgent;
import spiralcraft.io.TimestampFileSequence;
import spiralcraft.util.Path;
import spiralcraft.util.thread.CycleDetector;

public class RotatingFileHandler
  implements EventHandler
{
  
  private static final Formatter DEFAULT_FORMATTER
    =new DefaultFormatter();
  
  private Formatter formatter=DEFAULT_FORMATTER;

  private RotatingFileOutputAgent out=new RotatingFileOutputAgent();
  
  private ClassLog log=ClassLog.getInstance(RotatingFileHandler.class);
  
  private CycleDetector<RotatingFileHandler> cycleDetector
    =new CycleDetector<RotatingFileHandler>();
  
  public void setPath(Path path)
  {
    Path dir=path.parentPath();
    String name=path.lastElement();
    
    TimestampFileSequence fileSequence=new TimestampFileSequence();
    out.setFileSequence(fileSequence);
    
    // Activity logs should never use asyncIO.
    out.setAsyncIO(false);
    
    fileSequence.setDirectory(new File(dir.format("/")));
    fileSequence.setPrefix(name);
    fileSequence.setSuffix(".log");
    
  }
  
  public void setFormatter(Formatter formatter)
  { this.formatter=formatter;
  }
  
  @Override
  public void handleEvent(Event event)
  {
    
    try
    { out.write((formatter.format(event)+"\r\n").getBytes());
    }
    catch (IOException x)
    { 
      if (!cycleDetector.detectOrPush(this))
      {
        try
        {
          log.log(Level.SEVERE,"Error logging event..",x);
          log.log(event);
        }
        finally
        { cycleDetector.pop();
        }
      }
      else
      { x.printStackTrace();
      }
    }

    
  }

  @Override
  public void start()
    throws LifecycleException
  {
    out.start();
    handleEvent(Event.create(null,Level.INFO,"RotatingFileHandler started"));
  }

  @Override
  public void stop()
    throws LifecycleException
  { out.stop();
  }
  
  
  
  
}
