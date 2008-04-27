package spiralcraft.vfs.util;

import java.io.IOException;

import spiralcraft.time.Clock;
import spiralcraft.vfs.Resource;

/**
 * <P>Watches a Resource for updates and performs a task when the Resource is 
 *   updated. 
 * 
 * <P>The Resource will be checked for an update every pollInterval seconds.
 * 
 * <P>When an update is detected, the handleUpdate() method will be called.
 * 
 * <P>The result of the handleUpdate() method provides control over 
 *   the succeeding behavior.
 * 
 * @author mike
 */
public class Watcher
{

  protected final Resource resource;
  private final int pollIntervalMS;
  private final WatcherHandler handler;
  private long lastModified;
  private long lastChecked;
  private long holdOffUntil;
  private boolean firstTime=true;
  
  
  public Watcher(Resource resource,int pollIntervalMS,WatcherHandler handler)
  { 
    this.resource=resource;
    this.pollIntervalMS=pollIntervalMS;
    this.handler=handler;
    
  }
  
  /**
   * Set the watcher to ignore the latest changes
   */
  public synchronized void reset()
    throws IOException
  {
    lastChecked=Clock.instance().approxTimeMillis();
    lastModified=resource.getLastModified();
  }
  
  /**
   * Force a reload of the resource
   */
  public synchronized void refresh()
    throws IOException
  {
    this.lastModified=resource.getLastModified();
    int result=handler.handleUpdate();
    if (result>=0)
    { 
      this.lastModified=resource.getLastModified();
      if (result>0)
      { this.holdOffUntil=Clock.instance().approxTimeMillis()+result;
      }
    }
    else
    { 
      result=-result;
      this.holdOffUntil=Clock.instance().approxTimeMillis()+result;
    }
    this.lastChecked=Clock.instance().approxTimeMillis();

  }
  
  /**
   * Check if any action needs to be performed.
   */
  public synchronized void check()
    throws IOException
  {
    long time=Clock.instance().approxTimeMillis();
    
    if (time<holdOffUntil)
    { return;
    }
    if (time<lastChecked+pollIntervalMS)
    { return;
    }
    
    long lastModified=resource.getLastModified();
    if (lastModified!=this.lastModified || firstTime)
    { 
      firstTime=false;
      int result=handler.handleUpdate();
      if (result>=0)
      { 
        this.lastModified=resource.getLastModified();
        if (result>0)
        { this.holdOffUntil=Clock.instance().approxTimeMillis()+result;
        }
      }
      else
      { 
        result=-result;
        this.holdOffUntil=Clock.instance().approxTimeMillis()+result;
      }
    }  
    lastChecked=Clock.instance().approxTimeMillis();
  }

}
