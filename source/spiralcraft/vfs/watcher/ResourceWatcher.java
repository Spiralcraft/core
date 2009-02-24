package spiralcraft.vfs.watcher;

import java.io.IOException;

import spiralcraft.time.Clock;
import spiralcraft.vfs.Resource;

/**
 * <p>Watches a Resource for updates and calls a handler when the Resource is 
 *   updated. A Resource is considered updated solely on the basis of a
 *   change to the lastModified time.
 * </p>
 * 
 * <p>The Resource will be checked for an update every pollInterval seconds.
 * </p>
 * 
 * <p>When an update is detected, the handleUpdate() method will be called.
 * </p>
 * 
 * <p>The result of the handleUpdate() method provides optional feedback to
 *   adjust the polling interval.
 * </p>
 * 
 * @author mike
 */
public class ResourceWatcher
{

  protected final Resource resource;
  private final int pollIntervalMS;
  private final WatcherHandler handler;
  private long lastModified;
  private long lastChecked;
  private long holdOffUntil;
  private boolean firstTime=true;
  
  
  public ResourceWatcher
    (Resource resource
    ,int pollIntervalMS
    ,WatcherHandler handler
    )
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
   * Force a call to the handler as if the resource was modified
   */
  public synchronized void refresh()
    throws IOException
  {
    this.lastModified=resource.getLastModified();
    int result=handler.handleUpdate(resource);
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
      int result=handler.handleUpdate(resource);
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
