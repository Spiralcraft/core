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
package spiralcraft.data.util;

import java.util.ArrayList;
import java.util.LinkedList;

import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.command.CommandScheduler;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.session.DataSession;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.service.Service;

/**
 * <p>Asynchronously summarizes incoming data into persistent storage.
 * </p>
 * 
 * <p>Used to capture and record statistics on-the-fly
 * </p>
 * 
 * @author mike
 *
 */
public class SummarizerService<Tsummary extends Tuple,Tfact extends Tuple>
  extends AbstractComponent
  implements Service
{
  
  private final ClassLog log=ClassLog.getInstance(getClass());
  
  
  private LinkedList<Tfact> factQueue=new LinkedList<Tfact>();
  private ArrayList<Tfact> factBuffer=new ArrayList<Tfact>();
  private Summarizer<Tfact> summarizer=new Summarizer<Tfact>();
  
  private long idleDelayMs=1000;

  private ThreadLocalChannel<DataSession> sessionChannel
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
   * The data type which stores the summary
   * 
   * @param type
   */
  public void setSummaryType(Type<Tsummary> type)
  { summarizer.setSummaryType(type);
  }
  
  /**
   * The data type which contains the facts (detail) to tabulate
   * 
   * @param type
   */
  public void setFactType(Type<Tfact> type)
  { summarizer.setFactType(type);
  }
  
  
  public void setSummaryKeyBindings(Expression<?>[] summaryKeyBindings)
  { summarizer.setSummaryKeyBindings(summaryKeyBindings);
  }
  
  public void setSummaryDataBindings(Expression<?>[] summaryDataBindings)
  { summarizer.setSummaryDataBindings(summaryDataBindings);
  }
  
  public void setIdleDelayMs(int ms)
  { 
    this.idleDelayMs=ms;
    updater.setPeriod(ms);
  }
  
  /**
   * Called from client to accept a fact into the tabulation
   * 
   * @param fact
   */
  public void accept(Tfact fact)
  {
    synchronized (factQueue)
    { factQueue.add(fact);
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
  

  
  
  private synchronized void update()
  {
    synchronized (factQueue)
    {
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
      
        summarizer.dataInitialize(summarizer.getFactType().getFieldSet());
        try
        {
          for (Tfact fact:factBuffer)
          { summarizer.dataAvailable(fact);
          }
        }
        finally
        { summarizer.dataFinalize();
        }
  
        factBuffer.clear();
      }
      catch (DataException x)
      { 
        log.log
          (Level.SEVERE
          ,"Error updating summary "+summarizer.getSummaryType().getURI()
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
    summarizer.bind(focusChain);
    space=LangUtil.assertInstance(Space.class,focusChain);
    return focusChain;
  }


}
