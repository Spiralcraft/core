//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.io;

import spiralcraft.util.ByteBuffer;


import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.time.Scheduler;

import java.io.IOException;
import java.io.OutputStream;


/**
 * <p>Manages a data stream sent via the OutputStream interface. Contains a
 *   double buffering and an asynchronous scheduling facility. Provides an
 *   abstraction for implementing components that manage I/O like rotating
 *   log handlers, etc.
 * </p>
 * 
 * <p>Buffers data in a manner that maintains the integrity of the blocks of
 *   data written to it, and ensures that all buffered data is flushed
 *   within a specific time interval, without blocking the client thread on
 *   the low level IO process in async mode (as long as maxBufferSize and 
 *   maxDelayMs are set appropriately).
 * </p>
 * 
 * <p>Originally used to support writing log files as a non-blocking 
 *   background task.
 * </p>
 */
public abstract class OutputAgent
  extends OutputStream
  implements Runnable,Lifecycle
{
  protected final ClassLog log
    =ClassLog.getInstance(getClass());
  
  private final ByteBuffer[] _buffers={new ByteBuffer(),new ByteBuffer()};
  
  // _mutex synchronizes the double buffer 
  private volatile Object _mutex=new Object();  

  private volatile int _currentBuffer=0;
  
  private long maxDelayMs=1000;
  private long minDelayMs=0;
  private volatile boolean _initialized=false;
  private volatile boolean waiting=false;
  protected int maxBufferSize;
  private volatile boolean stopping=false;
  private volatile boolean stopped=false;
  private boolean asyncIO=true;
  
  private Thread shutdownHook=new Thread()
  { 
    @Override
    public void run() 
    { 
      synchronized(this) 
      {
        stopped=true;
        notify();
      } 
    };
  };
  
  /**
   * <p>Specify the maximum number of bytes that can accumulate in the buffer
   *   before future attempts to write data will be blocked. A value 
   *   less than or equal to zero (the default case) will put no limits on the
   *   size of the buffer. 
   * </p>
   * 
   * <p>Note that the size of the incoming data is not taken into account
   *   when determining when to block, and may exceed the value of 
   *   maxBufferSize.
   * </p>
   * 
   * @param maxBufferSize
   */
  public void setMaxBufferSize(int maxBufferSize)
  { this.maxBufferSize=maxBufferSize;
  }
  
  /**
   * <p>Whether the actual IO is performed asynchronously
   * 
   * @param val
   */
  public void setAsyncIO(boolean val)
  { this.asyncIO=val;
  }
  
  @Override
  public final void write(int val)
    throws IOException
  { write(new byte[] {new Integer(val).byteValue()},0,1);
  }

  @Override
  public final void write(final byte[] bytes)
    throws IOException
  { write(bytes,0,bytes.length);
  }
  
  @Override
  public final synchronized void
    write(final byte[] bytes,final int start,final int len)
    throws IOException
  { 
    assertInit();
    
    if (stopping)
    { throw new IOException("OutputAgent is stopping...");
    }    
      
    boolean blocked=false;
    synchronized (_mutex)
    { 

      while (maxBufferSize>0 
            && _buffers[_currentBuffer].length()>maxBufferSize
            && asyncIO
            )
      { 
        if (!blocked)
        {
          log.log
            (Level.WARNING,getLogPrefix()+": Blocking on full buffer "
            +_currentBuffer);
          blocked=true;
        }
        try
        { _mutex.wait();
        }
        catch (InterruptedException x)
        { 
          throw new IOException
            (getLogPrefix()+": Interrupted waiting for buffer "
            +_currentBuffer+" to empty");
        }
      }
      if (blocked)
      { log.log(Level.WARNING,getLogPrefix()+": Unblocking on "+_currentBuffer);
      }
      blocked=false;
      _buffers[_currentBuffer].append(bytes,start,len);
      waiting=true;
     
    }
    if (!asyncIO)
    { pump();
    }
  }
  
  @Override
  public final synchronized void flush()
    throws IOException
  {
    if (!asyncIO)
    { pump();
    }
  }
  
  public void setMaxDelayMs(long ms)
  { this.maxDelayMs=ms;
  }
  
  public void setMinDelayMs(long ms)
  { this.minDelayMs=ms;
  }
  

  @Override
  public synchronized void start()
    throws LifecycleException
  {
    
    _initialized=true;
    stopping=false;
    stopped=false;
    if (asyncIO)
    {
      Scheduler.instance().scheduleIn
        (this
        ,minDelayMs
        );
      
    }
    Runtime.getRuntime().addShutdownHook(shutdownHook);
    notify();
    
  }
  
  @Override
  public synchronized void stop()
  {
    if (!_initialized)
    { return;
    }
    _initialized=false;
    stopping=true;
    notify();
    
    if (asyncIO && !stopped)
    {
      try
      { 
        log.info(getLogPrefix()+": Waiting for flush...");
        wait();
      }
      catch (InterruptedException x)
      {
      }
    }
    Runtime.getRuntime().removeShutdownHook(shutdownHook);
    destroy();
  }

  /**
   * De-allocate any resources
   */
  protected abstract void destroy();
  
  /**
   * Ensure that we are ready to write output to the appropriate place
   */
  protected abstract void prepare()
    throws IOException;
  
  protected abstract void output(byte[] bytes)
    throws IOException;

  protected abstract String getLogPrefix();
  
  public void run()
  {
    try
    {
      
      pump();
      
      long delay;
      synchronized (_mutex)
      { delay=waiting?minDelayMs:maxDelayMs;
      }
        
      boolean end;
      synchronized (this)
      { end=stopping;
      }
      // After this point, nothing should be coming in, and stop() should
      //   be waiting.
      
      if (!end)
      {
        Scheduler.instance().scheduleIn
          (this
          ,delay
          );
      }
      else
      {
        pump();
        pump();
        synchronized(this)
        { notify();
        }
        stopped=true;
      }
    }
    catch (Exception x)
    { 
      // Back off for a minute
      x.printStackTrace();
      
      // Note- could cause deadlock if log we are writing to uses this
      //   output agent.
      log.log(Level.SEVERE,getLogPrefix()+"Error writing output",x);

      boolean end;
      synchronized (this)
      { end=stopping;
      }
      
      if (!end)
      {
        Scheduler.instance().scheduleIn
          (this
          ,60000
          );
      }
      else
      {
        synchronized(this)
        { 
          notify();
          stopped=true;
        }
      }
      
    }

  }
  
  protected final void pump()
    throws IOException
  { 
    prepare();
    
    int flushBuffer;
    
    synchronized (_mutex)
    { 
      // Swap buffers
      flushBuffer=_currentBuffer;
      _currentBuffer=(_currentBuffer==0?1:0);
      waiting=false;
      _mutex.notify();
    }
    
    output(_buffers[flushBuffer].toByteArray());
    _buffers[flushBuffer].clear();
    
  }
  
  
  private void assertInit()
  { 
    if (!_initialized)
    { 
      synchronized (this)
      { 
        if (!_initialized)
        { 
          try
          { wait(10000);
          }
          catch (InterruptedException x)
          { 
            throw new RuntimeException
              ("Interrupted or timed out waiting for initialization");
          }
        }
      }
      if (!_initialized)
      { throw new RuntimeException
          ("RotatingFileAccessLog has not been initialized");
      }
    }
  }

  
}
