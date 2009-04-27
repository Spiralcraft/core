//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.data.query;

import spiralcraft.common.LifecycleException;
import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;

/**
 * Performs a query within the context of the Task framework.
 * 
 * @author mike
 *
 */
public class QueryScenario
  extends Scenario<QueryScenario.QueryTask,Aggregate<Tuple>>
{
  
  protected Query query;
  protected BoundQuery<?,Tuple> boundQuery;
  protected Scenario<?,?> scenario;
  protected ThreadLocalChannel<Aggregate<Tuple>> resultChannel;
  

  public class QueryTask
    extends AbstractTask<Aggregate<Tuple>>
  {

    @Override
    protected void work()
      throws InterruptedException
    { 
      try
      {
        SerialCursor<Tuple> cursor=boundQuery.execute();
        if (debug)
        { log.fine("Got "+cursor);
        }
        
        CursorAggregate<Tuple> result=new CursorAggregate<Tuple>(cursor);
        if (scenario!=null)
        {
          resultChannel.push(result);
        
          try
          { executeChild(scenario);
          }
          finally
          { resultChannel.pop();
          }
        }
        addResult(result);
      }
      catch (DataException x)
      { 
        if (debug)
        { log.log(Level.WARNING,"Threw",x);
        }
        addException(x);
      }
    }
  };
  
  @Override
  protected QueryTask task()
  { return new QueryTask();
  }

  public void setScenario(Scenario<?,?> scenario)
  { this.scenario=scenario;
  }
  
  /**
   * The Query to run
   * 
   * @param query
   */
  public void setQuery(Query query)
  { this.query=query;
  }
  
  @Override
  public Focus<?> bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    
    Space space
      =focusChain.<Space>findFocus(Space.SPACE_URI).getSubject().get();
    try
    { boundQuery=space.query(query,focusChain);
    }
    catch (DataException x)
    { throw new BindException("Error binding query",x);
    }
    if (scenario!=null)
    { 
      resultChannel
        =new ThreadLocalChannel<Aggregate<Tuple>>
          (DataReflector.<Aggregate<Tuple>>getInstance
            (Type.getAggregateType(boundQuery.getType()))
          );
      focusChain=focusChain.chain(resultChannel);
      scenario.bind(focusChain);
    }
    
    return focusChain;
  }

  @Override
  public void start()
    throws LifecycleException
  { 
    super.start();
    if (scenario!=null)
    { scenario.start();
    }
  }
  
  @Override
  public void stop()
    throws LifecycleException
  {
    if (scenario!=null)
    { scenario.stop();
    }
    super.stop();
  }
  
}
