//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.pool;

import java.util.Stack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.callable.Sink;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

import spiralcraft.time.Clock;


public class Pool<T>
  implements Lifecycle
{

  private static int NEXT_ID=0;
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level debugLevel
    =ClassLog.getInitialDebugLevel(getClass(),null);
  
  private int _overdueSeconds=600;
  private ResourceFactory<T> _factory;
  private int _idleSeconds=3600;
  private int _maxCapacity=Integer.MAX_VALUE;
  private int _initialSize=1;
  private int _minAvailable=1;
  private volatile long _lastUse=0;
  private long _maintenanceInterval=500;
  private Keeper _keeper=new Keeper();
  private Stack<Reference<T>> _available
    =new Stack<Reference<T>>();
  private HashMap<Object,Reference<T>> _out
    =new HashMap<Object,Reference<T>>();
  private Object _monitor=new Object();
  private volatile boolean _started=false;
  // private Object _startLock=new Object();
  private boolean _conserve=false;
  private long _maxStartupMs;
  private long _maxCheckoutMs;
  
  private int _checkedInCount;
  private int _checkedOutCount;
  private int _checkInsCount;
  private int _checkOutsCount;
  private int _clientDiscardsCount;
  private int _waitsCount;
  private volatile int _waitingCount;
  private int _overdueDiscardsCount;
  private int _addsCount;
  private int _removesCount;
  private Sink<T> onCheckout;
  private Sink<T> onCheckin;


  /**
   * Conserve resources by not discarding them when demand drops,
   *   in order to promote maximum reuse.
   */
  public void setConserve(boolean val)
  { _conserve=val;
  }

  /**
   * 
   * @param ms The amount of time to wait when on checkout when
   *   no resources are available
   */
  public void setMaxCheckoutMs(long ms)
  { this._maxCheckoutMs=ms;
  }
  
  /**
   * 
   * @param ms The amount of time to allow the pool to restore
   *   to the initial state before returning from start
   */
  public void setMaxStartupMs(long ms)
  { this._maxStartupMs=ms;
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
  public void setResourceFactory(ResourceFactory<T> factory)
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
  @Override
  public void start()
  {
    synchronized (_monitor)
    {
      _started=true;
      restoreInitial(_maxStartupMs);
      _keeper.start();
      _monitor.notifyAll();
      
    }
    
  }

  /**
   * Stop the pool and discard all resources
   */
  @Override
  public void stop()
  {
    synchronized (_monitor)
    {
      _started=false;
      _keeper.stop();
      while (!_available.isEmpty())
      { _factory.discardResource( popAvailable().resource );
      }
      if (!_out.isEmpty())
      {
        log.warning("Discarding "+_out.size()+" checked out entries");
        for (Reference<T> ref:_out.values())
        { _factory.discardResource(ref.resource);
        }
        _out.clear();
      }
    }
  }

  public int getTotalSize()
  { return _available.size()+_out.size();
  }

  public int getNumAvailable()
  { return _available.size();
  }

  public void setOnCheckout(Sink<T> onCheckout)
  { this.onCheckout=onCheckout;
  }

  public void setOnCheckin(Sink<T> onCheckin)
  { this.onCheckin=onCheckin;
  }
  
  /**
   * Checkout an object from the pool of
   *   available object.
   */
  public T checkout()
    throws InterruptedException
  {
    long lastUse=Clock.instance().approxTimeMillis();
    T ret=null;
    synchronized (_monitor)
    {
      _lastUse=lastUse;
      if (!_started)
      { 
        if (debugLevel.isDebug())
        { log.debug("Waiting for pool to start");
        }
        waitOnMonitor();
        if (debugLevel.isDebug())
        { log.debug("Notified that pool started");
        }
      }

      while (_available.isEmpty() && _started)
      { 
        _keeper.wake();
        if (_available.isEmpty())
        {
          try
          { 
            _waitsCount++;
            _waitingCount++;
            log.info("Waiting for pool");
            long time=System.currentTimeMillis();
            waitOnMonitor();
            log.info("Waited "+(System.currentTimeMillis()-time)+" for pool");
          }
          finally
          { _waitingCount--;
          }
        }
      }
      
      if (_started)
      {
        Reference<T> ref=popAvailable();
        ref.checkOutTime=Clock.instance().approxTimeMillis();
        putOut(ref);
        _checkOutsCount++;

        if (debugLevel.isFine())
        { log.fine("Checkout complete");
        }
        ret=ref.resource;
      }
      else
      { throw new InterruptedException("Pool is stopping");
      }

    }
    
    if (onCheckout!=null)
    { 
      try
      { onCheckout.accept(ret);
      }
      catch (Exception x)
      { log.log(Level.WARNING,"Error running onCheckout",x);
      }
    }
    return ret;
  }

  private void waitOnMonitor()
    throws InterruptedException
  {
    if (_maxCheckoutMs>0)
    {
      long start=System.currentTimeMillis();
      _monitor.wait(_maxCheckoutMs);
      if (System.currentTimeMillis()-start>=_maxCheckoutMs)
      { 
        throw new InterruptedException
          ("Checkout timed out after "+_maxCheckoutMs+"ms");
      }
    }
    else
    { _monitor.wait();
    }
    
  }
  /**
   * Return a checked out object to the pool
   *   of available objects.
   */
  public void checkin(T resource)
  {
    
    if (onCheckin!=null)
    { 
      try
      { onCheckin.accept(resource);
      }
      catch (Exception x)
      { log.log(Level.WARNING,"Error running onCheckin",x);
      }
    }
    
    _lastUse=Clock.instance().approxTimeMillis();
    _checkInsCount++;
    synchronized (_monitor)
    {
      Reference<T> ref=removeOut(resource);
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
      { log.log(Level.WARNING,"Unbalanced checkin: "+resource.toString()); 
      }
    }

    
  }


  /**
   * Discard a checked out object. A new object will be created to
   *   fill the void if required. Used when it is known that an
   *   object is corrupt or has expired for some reason.
   */
  public void discard(T resource)
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
    private int _numTimes=0;
    private Object _keeperMonitor=new Object();
    private String name="PoolKeeper-"+NEXT_ID++;
    private volatile boolean _requested=false;

    public void stop()
    { 
      _done=true;
      synchronized (_keeperMonitor)
      { _keeperMonitor.notify();
      }
    }

    public void work()
    {

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
            { restoreInitial(0);
            }
          }
          else
          { grow();
          }
        }
        catch (Exception x)
        { log.warning(name+": "+x.toString());
        }
      }
      else
      { log.log(Level.SEVERE,"No factory installed");
      }
      
    }

    @Override
    public void run()
    {
      
      while (!_done)
      {
        
        // Keep work outside the scope of the lock
        work();
        
        while (_requested && !_done)
        { 
          // A request went unsatisfied while we were doing work
          //   make sure we stay ahead of the queue
          synchronized(_keeperMonitor)
          { _requested=false;
          }
          work();
        }
        
        if (_done)
        { break;
        }
        
        synchronized(_keeperMonitor)
        {
          try
          { 
            _keeperMonitor.wait(_maintenanceInterval);
          }
          catch (InterruptedException x)
          { 
            log.log(Level.WARNING,"Pool keeper interrupted",x);
            
          }
          _requested=false;
        }        
        
      }
    }
    
    public void start()
    { 
      synchronized (_keeperMonitor)
      { 
        Thread keeperThread=new Thread(this,name);
        keeperThread.setDaemon(true);
        keeperThread.setPriority(keeperThread.getPriority()+1);
        keeperThread.start();
      }
    }
    
    public void wake()
    { 
      synchronized (_keeperMonitor)
      { 
        _requested=true;
        _keeperMonitor.notify();
      }
    }
  }



  private void putOut(Reference<T> ref)
  { 
    _out.put(ref.resource,ref);
    _checkedOutCount++;
  }

  private Reference<T> removeOut(Object res)
  { 
    Reference<T> ref=_out.remove(res);
    if (ref!=null)
    { _checkedOutCount--;
    }
    return ref;
  }

  private void restoreInitial(long maxWait)
  {
    long start=System.currentTimeMillis();
    
    while (getTotalSize()<_initialSize
           && _started
           && (maxWait==0
               || System.currentTimeMillis()-start<maxWait
               )
          )
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
    // Include _waitingCount in computation to stay ahead of rush
    while ( ( getNumAvailable() - _waitingCount) <_minAvailable
            && getTotalSize()<_maxCapacity 
            && _started
          )
    { add();
    }
  }

  @SuppressWarnings({"unchecked","rawtypes"})
  private void discardOverdue()
  {

    
    Reference<T>[] snapshot;
    synchronized (_monitor)
    { 
      Collection<Reference<T>> collection=_out.values();
      snapshot=new Reference[collection.size()];
      collection.toArray(snapshot);
    }

    List<T> discardList=null;
    long time=Clock.instance().approxTimeMillis();

    for (int i=0;i<snapshot.length;i++)
    {
      if (snapshot[i].checkOutTime-time>_overdueSeconds*1000)
      {
        Reference<T> ref=null;
        synchronized (_monitor)
        { ref=removeOut(snapshot[i].resource);
        }
        if (ref!=null)
        { 
          if (discardList==null)
          { discardList=new LinkedList<T>();
          }
          discardList.add(ref.resource);
        }
      }
    }

    if (discardList!=null)
    {
      Iterator<T> it=discardList.iterator();
      while (it.hasNext())
      { 
        _overdueDiscardsCount++;
        _factory.discardResource(it.next()); 
      }
    }
    
  }

  private void add()
  {
    Reference<T> ref=new Reference<T>();
    try
    { ref.resource=_factory.createResource();
    }
    catch (Throwable x)
    { log.log(Level.SEVERE,"Exception creating pooled resource. ",x);
    }

    if (ref.resource!=null)
    {
      synchronized (_monitor)
      { 
        pushAvailable(ref);
        _monitor.notify();
      }
      _addsCount++;

      if (debugLevel.isDebug())
      { log.fine("Added resource "+ref.resource.getClass().getName());
      }
    }
  }

  private Reference<T> popAvailable()
  {
    Reference<T> ret=_available.pop();
    _checkedInCount--;
    return ret;
  }

  private void pushAvailable(Reference<T> ref)
  {
    _available.push(ref);
    _checkedInCount++;
  }

  private void remove()
  {
    T resource=null;

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
      { log.log(Level.WARNING,"Exception discarding pooled resource. ",x);
      }
      _removesCount++;
    }
  }
}



class Reference<X>
{
  public X resource;
  public long checkOutTime;
}