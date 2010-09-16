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
package spiralcraft.data.task;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.CursorChannel;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;
import spiralcraft.task.Chain;

/**
 * Performs a query within the context of the Task framework.
 * 
 * @author mike
 *
 */
public class Fetch<Tcontext>
  extends Chain<Tcontext,Aggregate<Tuple>>
{
  
  protected Query query;
  protected BoundQuery<?,Tuple> boundQuery;
  protected ThreadLocalChannel<Aggregate<Tuple>> resultChannel;
  protected Expression<Tuple> cursorX;
  @SuppressWarnings("rawtypes")
  protected CursorChannel cursorChannel;
  protected int batchSize;
  protected Queryable<?> queryable;
  
  { storeResults=true;
  }

  public Fetch()
  {
  }
  
  public Fetch(Expression<Tcontext> contextX,Query query)
  { 
    this.contextX=contextX;
    this.query=query;
  }
  
  public Fetch(Query query)
  { this.query=query;
  }
  
  public Fetch(Queryable<?> queryable,Query query)
  { 
    this.query=query;
    this.queryable=queryable;
  }
  
  public class FetchTask
    extends ChainTask
  {

    @Override
    protected void work()
      throws InterruptedException
    { 
      try
      {
        SerialCursor<Tuple> cursor=boundQuery.execute();
          
        try
        {
          if (debug)
          { log.fine("Got "+cursor);
          }

          if (cursorChannel!=null)
          { 
            
            cursorChannel.setCursor(cursor);
            super.work();
          }
          else
          {
          
            boolean done=false;
            while (!done)
            {
              CursorAggregate<Tuple> result
                =batchSize>0
                ?new CursorAggregate<Tuple>(cursor,batchSize)
                :new CursorAggregate<Tuple>(cursor)
                ;
              resultChannel.push(result);
        
              try
              { super.work();
              }
              finally
              { resultChannel.pop();
              }
              addResult(result);

              if (batchSize==0)
              { done=true;
              }
              else
              { done=result.size()==0;
              }
 
            }
          
          }
        }
        finally
        { cursor.close();
        }
      }
      catch (DataException x)
      { 
        if (debug)
        { log.log(Level.WARNING,"Threw",x);
        }
        addException(x);
      }
    }
  }
  
  /**
   * Specify the number of results to send down the chain for each
   *   iteration. The default value of 0 sends the entire result.
   * 
   * @param batchSize
   */
  public void setBatchSize(int batchSize)
  { this.batchSize=batchSize;
  }
  
  @Override
  protected FetchTask task()
  { return new FetchTask();
  }

  
  /**
   * The Query to run
   * 
   * @param query
   */
  public void setQuery(Query query)
  { this.query=query;
  }
  
  public void setCursorX(Expression<Tuple> cursorX)
  { this.cursorX=cursorX;
  }
  
  @Override
  public Focus<?> bindImports(Focus<?> focusChain)
    throws BindException
  {
    if (query==null)
    { throw new BindException("Query cannot be null");
    }
    try
    { query.resolve();
    }
    catch (DataException x)
    { throw new BindException("Error resolving query "+x);
    }
    Type<?> type=query.getType();
    if (type!=null)
    { resultReflector=DataReflector.getInstance(Type.getAggregateType(type));
    }
    return focusChain;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Focus<?> bindExports(
    Focus<?> focusChain)
    throws BindException
  {
    
    Focus<Queryable<Tuple>> queryableFocus
      =focusChain.<Queryable<Tuple>>findFocus
        (Queryable.QUERYABLE_URI);
    
    Queryable queryable=this.queryable;
    if (queryable==null && queryableFocus!=null)
    { queryable=queryableFocus.getSubject().get();
    }
   
    if (queryable!=null)
    {
      try
      { boundQuery=queryable.query(query,focusChain);
      }
      catch (DataException x)
      { throw new BindException("Error binding query",x);
      }
    }
    else
    { 
      try
      { boundQuery=(BoundQuery<?,Tuple>) query.bind(focusChain);
      }
      catch (DataException x)
      { 
        throw new BindException
          ("Error obtaining default binding for query "+query
          ,x
          ); 
      }
    }
    
    if (resultReflector==null)
    { 
      resultReflector
        =DataReflector.getInstance(Type.getAggregateType(boundQuery.getType()));
    }

    if (cursorChannel!=null)
    {
      cursorChannel
        =(CursorChannel) focusChain.bind(cursorX);
    }
    
    resultChannel
      =new ThreadLocalChannel<Aggregate<Tuple>>
        (DataReflector.<Aggregate<Tuple>>getInstance
          (Type.getAggregateType(boundQuery.getType()))
        );
        
// Too late for this here
//    resultReflector=resultChannel.getReflector();

    focusChain=focusChain.chain(resultChannel);
    if (debug)
    { log.fine("Fetch Context is "+resultChannel.getContext());
    }
    return focusChain;
  }


  
}
