package spiralcraft.pool;

import java.util.Stack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import spiralcraft.time.Clock;
import spiralcraft.time.Scheduler;

import java.util.logging.Logger;
import java.util.logging.Level;

public class Pool
{

  private int _overdueSeconds=600;
  private ResourceFactory _factory;
  private int _idleSeconds=3600;
  private int _maxCapacity=Integer.MAX_VALUE;
  private int _initialSize=1;
  private int _minAvailable=1;
  private long _lastUse=0;
  private long _maintenanceInterval=500;
  private Keeper _keeper=new Keeper();
  private Stack _available=new Stack();
  private HashMap _out=new HashMap();
  private Object _monitor=new Object();
  private Logger _log;
  private boolean _started=false;
  private Object _startLock=new Object();
  private boolean _conserve=false;

  private int _checkedInCount;
  private int _checkedOutCount;
  private int _checkInsCount;
  private int _checkOutsCount;
  private int _clientDiscardsCount;
  private int _waitsCount;
  private int _waitingCount;
  private int _overdueDiscardsCount;
  private int _addsCount;
  private int _removesCount;

  public void register(RegistryNode node)
  { _log=(Logger) node.findInstance(Logger.class);
  }

  /**
   * Conserve resources by not discarding them when demand drops,
   *   in order to promote maximum reuse.
   */
  public void setConserve(boolean val)
  { _conserve=val;
  }

  /**
   * Specify the initial number of objects that
   *   will be created.
   */
  public void setInitialSize(int size)
  { 
    if (size<=0)
    { throw new IllegalArgumentException("Initial size of pool must be at least 1");
    }
    _initialSize=size;
  }

  public void setLogger(Logger logger)
  { _log=logger;
  }

  /**
   * Specify the minimum number of available
   *   objects. When the number of available
   *   objects crosses below this threshold, new 
   *   objects will be added to the pool until
   *   this threshold is reached.
   */
  public void setMinAvailable(int size)
  { 
    if (size<=0)
    { throw new IllegalArgumentException("Minimum available objects in pool must be at least 1");
    }
    _minAvailable=size;
  }

  /**
   * Specify the maximum total of checked
   *   out and available items.
   */
  public void setMaxCapacity(int size)
  { _maxCapacity=size;
  }

  /**
   * Specify the time of no activity after which the pool
   *   will to the initial size.
   */
  public void setIdleTimeSeconds(int seconds)
  { _idleSeconds=seconds;
  }

  /**
   * Specify the component that creates and discards 
   *   pooled objects.
   */
  public void setResourceFactory(ResourceFactory factory)
  { _factory=factory;
  }
  
  /**
   * Specify the maximum check-out time, after which
   *   an item will be discarded.
   */
  public void setOverdueTimeSeconds(int seconds)
  { _overdueSeconds=seconds;
  }

  /**
   * Start the pool by filling it up to the minumum size and
   *   starting the Keeper.
   */
  public void init()
  {
    synchronized (_monitor)
    {
      restoreInitial();
      _started=true;
      _keeper.start();
      _monitor.notifyAll();
      
    }
    
  }

  /**
   * Stop the pool and discard all resources
   */
  public void stop()
  {
    synchronized (_monitor)
    {
      _started=false;
      _keeper.stop();
      while (!_available.isEmpty())
      { _factory.discardResource( popAvailable().resource );
      }
    }
  }

  public int getTotalSize()
  { return _available.size()+_out.size();
  }

  public int getNumAvailable()
  { return _available.size();
  }

  /**
   * Checkout an object from the pool of
   *   available object.
   */
  public Object checkout()
  {
    _lastUse=Clock.instance().approxTimeMillis();
    synchronized (_monitor)
    {
      if (!_started)
      { 
        logFine("Waiting for pool to start");
        try
        { 
          _monitor.wait();
          logFine("Notified that pool started");
        }
        catch (InterruptedException x)
        {  
          logWarning("Checkout on startup interrupted");
          return null;
        }
      }

      while (_available.isEmpty())
      { 
        _keeper.wake();
        if (_available.isEmpty())
        {
          try
          { 
            _waitsCount++;
            _waitingCount++;
            logInfo("Waiting for pool");
            long time=System.currentTimeMillis();
            _monitor.wait();
            _waitingCount--;
            logInfo("Waited "+(System.currentTimeMillis()-time)+" for pool");
          }
          catch (InterruptedException x)
          { 
            _waitingCount--;
            return null; 
          }
        }
      }
      Reference ref=popAvailable();
      ref.checkOutTime=Clock.instance().approxTimeMillis();
      putOut(ref);
      _checkOutsCount++;
      return ref.resource;

    }
  }


  /**
   * Return a checked out object to the pool
   *   of available objects.
   */
  public void checkin(Object resource)
  {
    _lastUse=Clock.instance().approxTimeMillis();
    _checkInsCount++;
    synchronized (_monitor)
    {
      Reference ref=removeOut(resource);
      if (ref!=null)
      {
        if (_started)
        { pushAvailable(ref);
        }
        else
        { _factory.discardResource(resource);
        }
        _monitor.notify();
      }
      else
      { logWarning("Unbalanced checkin: "+resource.toString()); 
      }
    }

  }


  /**
   * Discard a checked out object. A new object will be created to
   *   fill the void if required. Used when it is known that an
   *   object is corrupt or has expired for some reason.
   */
  public void discard(Object resource)
  {
    _lastUse=Clock.instance().approxTimeMillis();
    synchronized (_monitor)
    { removeOut(resource);
    }
    _clientDiscardsCount++;
    _factory.discardResource(resource);
  }

  //////////////////////////////////////////////////////////////////
  //
  // Private Members
  //
  //////////////////////////////////////////////////////////////////

  class Keeper
    implements Runnable
  {
    private boolean _done=false;
    private boolean _running=false;
    private int _numTimes=0;
    private Object _keeperMonitor=new Object();
    private int _scheduledCount=0;

    public void stop()
    { _done=true;
    }

    public void run()
    {
      if (_done)
      { return;
      }

      synchronized (_keeperMonitor)
      { 
        _scheduledCount--;
        if (_running)
        { return;
        }
        _running=true;
      }

      if (_factory!=null)
      {
        _numTimes++;
        try
        {
          if (_overdueSeconds>0)
          { discardOverdue();
          }
  
          if ( System.currentTimeMillis()-_lastUse>(_idleSeconds*1000) )
          {
            if ( getTotalSize()>_initialSize)
            { restoreInitial();
            }
          }
          else
          { grow();
          }
        }
        catch (Exception x)
        { 
          if (_log!=null)
          { _log.warning("PoolKeeper: "+x.toString());
          }
        }
      }
      else
      { logSevere("No factory installed");
      }
      
      synchronized (_keeperMonitor)
      {
        _running=false;
        if (_scheduledCount==0)
        {
          _scheduledCount++;
          Scheduler.instance().scheduleIn(this,_maintenanceInterval);
        }
      }
    }

    public void start()
    { 
      synchronized (_keeperMonitor)
      {
        _scheduledCount++;
        Scheduler.instance().scheduleNow(this);
      }
    }
    
    public void wake()
    { 
      synchronized (_keeperMonitor)
      {
        if (!_running && _scheduledCount<2)
        { 
          _scheduledCount++;
          Scheduler.instance().scheduleNow(this);
        }
      }
    }
  }

  class Reference
  {
    public Object resource;
    public long checkOutTime;
  }


  private void putOut(Reference ref)
  { 
    _out.put(ref.resource,ref);
    _checkedOutCount++;
  }

  private Reference removeOut(Object res)
  { 
    Reference ref=(Reference) _out.remove(res);
    if (ref!=null)
    { _checkedOutCount--;
    }
    return ref;
  }

  private void restoreInitial()
  {
    while (getTotalSize()<_initialSize)
    { add();
    }
    if (!_conserve)
    {
      while (getTotalSize()>_initialSize && getNumAvailable()>0)
      { remove();
      }
    }
  }

  private void grow()
  {
    while (getNumAvailable()<_minAvailable && getTotalSize()<_maxCapacity && _started==true)
    { add();
    }
  }

  private void discardOverdue()
  {

    
    Reference[] snapshot;
    synchronized (_monitor)
    { 
      Collection collection=_out.values();
      snapshot=new Reference[collection.size()];
      collection.toArray(snapshot);
    }

    List discardList=null;
    long time=Clock.instance().approxTimeMillis();

    for (int i=0;i<snapshot.length;i++)
    {
      if (snapshot[i].checkOutTime-time>_overdueSeconds*1000)
      {
        Reference ref=null;
        synchronized (_monitor)
        { ref=removeOut(snapshot[i].resource);
        }
        if (ref!=null)
        { 
          if (discardList==null)
          { discardList=new LinkedList();
          }
          discardList.add(ref.resource);
        }
      }
    }

    if (discardList!=null)
    {
      Iterator it=discardList.iterator();
      while (it.hasNext())
      { 
        _overdueDiscardsCount++;
        _factory.discardResource(it.next()); 
      }
    }
    
  }

  private void add()
  {
    logFine("Adding resource");
    Reference ref=new Reference();
    try
    { ref.resource=_factory.createResource();
    }
    catch (Throwable x)
    { logSevere("Exception creating pooled resource. ",x);
    }

    if (ref.resource!=null)
    {
      synchronized (_monitor)
      { 
        pushAvailable(ref);
        _monitor.notify();
      }
      _addsCount++;
    }
  }

  private Reference popAvailable()
  {
    Reference ret=(Reference) _available.pop();
    _checkedInCount--;
    return ret;
  }

  private void pushAvailable(Reference ref)
  {
    _available.push(ref);
    _checkedInCount++;
  }

  private void remove()
  {
    Object resource=null;

    synchronized (_monitor)
    {
      if (!_available.isEmpty())
      { resource=popAvailable().resource;
      }
    }
    
    if (resource!=null)
    {
      try
      { _factory.discardResource(resource);
      }
      catch (Exception x)
      { logWarning("Exception discarding pooled resource. ",x);
      }
      _removesCount++;
    }
  }

  private void logWarning(String message)
  { 
    if (_log!=null)
    { _log.warning(message);
    }
  }

  private void logWarning(String message,Exception x)
  { 
    if (_log!=null)
    { _log.warning(message+": "+x.toString());
    }
  }


  private void logSevere(String message)
  { 
    if (_log!=null)
    { _log.severe(message);
    }
  }

  private void logSevere(String message,Throwable x)
  { 
    if (_log!=null)
    { _log.severe(message+": "+x.toString());
    }
  }

  private void logInfo(String message)
  { 
    if (_log!=null)
    { _log.info(message);
    }
  }

  private void logFine(String message)
  { 
    if (_log!=null)
    { _log.fine(message);
    }
  }

  private void logFinest(String message)
  { 
    if (_log!=null)
    { _log.finest(message);
    }
  }
}


