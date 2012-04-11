//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.data.kit;

import java.util.ArrayList;
import java.util.LinkedList;

import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.command.CommandScheduler;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.callable.Sink;
import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Type;
import spiralcraft.data.session.DataSession;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.Level;
import spiralcraft.service.Service;
import spiralcraft.time.Clock;

/**
 * <p>Asynchronously queues and batch-processes incoming data/objects 
 * </p>
 * 
 * @author mike
 *
 */
public abstract class AbstractBatchService<Tdata>
  extends AbstractComponent
  implements Service,Sink<Tdata>
{
  private LinkedList<Tdata> factQueue=new LinkedList<Tdata>();
  private ArrayList<Tdata> factBuffer=new ArrayList<Tdata>();
  private Type<Tdata> dataType;
  
  private long idleDelayMs=1000;
  private long sizeThreshold=1;
  private long ageThresholdMs=1000;
  private long queueStartTime;

  protected final ThreadLocalChannel<DataSession> sessionChannel
    =new ThreadLocalChannel<DataSession>
      (BeanReflector.<DataSession>getInstance(DataSession.class));
  
  private final CommandScheduler updater
    =new CommandScheduler
    (idleDelayMs
    ,new Runnable()
      {
        @Override
        public void run()
        { 
          if (!running)
          { return;
          }
          update();
          updater.setPeriod(idleDelayMs);
        }
      }
    );
  
  private boolean running;
  
  private Object monitor=new Object();
  
  private Space space;
  
  /**
   * The data type of the input data
   * 
   * @param type
   */
  public void setDataType(Type<Tdata> dataType)
  { this.dataType=dataType;
  }

  public Type<Tdata> getDataType()
  { return dataType;
  }

  /**
   * Specify how often the queue will be checked
   * 
   * @param ms
   */
  public void setIdleDelayMs(int ms)
  { 
    this.idleDelayMs=ms;
    updater.setPeriod(ms);
  }
  

  /**
   * Specify how old a non-empty queue can get before processing is
   *   triggered. The default is 1 second.
   * 
   * @param ms
   */
  public void setAgeThresholdMs(long ms)
  { this.ageThresholdMs=ms;
  }

  /**
   * <p>Specify how big a queue can get before processing is triggered. The
   *   default is 1.
   * </p>
   * 
   * 
   * 
   * @param sizeThreshold
   */
  public void setSizeThreshold(long sizeThreshold)
  { this.sizeThreshold=sizeThreshold;
  }

  /**
   * Called from client to accept a fact into the tabulation
   * 
   * @param fact
   */
  @Override
  public void accept(Tdata fact)
  {
    synchronized (factQueue)
    { 
      if (factQueue.isEmpty())
      { queueStartTime=Clock.instance().approxTimeMillis();
      }
      factQueue.add(fact);
    }
  }
  
  @Override
  public void start()
    throws LifecycleException
  {

    super.start();
    synchronized (monitor)
    { 
      running=true;
      updater.start();
    }
    
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    synchronized(monitor)
    {
      running=false;
      updater.stop();
    }
    super.stop();
    
  }
  

  protected abstract void processBatch(ArrayList<Tdata> factBuffer)
    throws DataException;
  
  private synchronized void update()
  {
    synchronized (factQueue)
    {
      if (running 
          && factQueue.size()<sizeThreshold 
          && (factQueue.isEmpty()
              || Clock.instance().approxTimeMillis()-queueStartTime
                   <ageThresholdMs
             )
          )
      { 
        // Skip the update if the queue is undersized and the age
        //   threshold hasn't been met
        return;
      }
      
      factBuffer.ensureCapacity(factQueue.size());
      factBuffer.addAll(factQueue);
      factQueue.clear();

    }
    
    if (!factBuffer.isEmpty())
    {
      DataSession session=new DataSession();
      session.setSpace(space);
      sessionChannel.push(session);
      
      try
      {
        processBatch(factBuffer);  
        factBuffer.clear();
      }
      catch (Exception x)
      { 
        log.log
          (Level.SEVERE
          ,"Error processing batch of "+dataType.getURI()
          ,x
          );
      }
      finally
      { sessionChannel.pop();
      }
    }
    
    synchronized(monitor)
    { monitor.notifyAll();
    }

  }

  public void flush()
  { 
    try
    { 
      synchronized(monitor)
      {
        while (!factQueue.isEmpty() || !factBuffer.isEmpty())
        {
          if (running)
          { monitor.wait(1000);
          }
        }
      }
    }
    catch (InterruptedException x)
    { log.log(Level.WARNING,"Timed out",x);
    }
  }
  
  
  @Override
  protected Focus<?> bindImports(
    Focus<?> focusChain)
    throws ContextualException
  { 
    focusChain=super.bindImports(focusChain);
    
    focusChain=focusChain.chain(sessionChannel);
    space=LangUtil.assertInstance(Space.class,focusChain);
    return focusChain;
  }


}
