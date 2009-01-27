package spiralcraft.log;

import java.io.File;
import java.io.IOException;

import spiralcraft.common.LifecycleException;
import spiralcraft.io.RotatingFileOutputAgent;
import spiralcraft.util.Path;

public class RotatingFileHandler
  implements EventHandler
{
  
  private static final Formatter DEFAULT_FORMATTER
    =new DefaultFormatter();
  
  private Formatter formatter=DEFAULT_FORMATTER;

  private RotatingFileOutputAgent out=new RotatingFileOutputAgent();
  
  private ClassLog log=ClassLog.getInstance(RotatingFileHandler.class);
  
  public void setPath(Path path)
  {
    Path dir=path.parentPath();
    String name=path.lastElement();
    out.setDirectory(new File(dir.format("/")));
    out.setFilePrefix(name);
    out.setFileSuffix("log");
    
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
      log.log(Level.SEVERE,"Error logging event..",x);
      log.log(event);
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
