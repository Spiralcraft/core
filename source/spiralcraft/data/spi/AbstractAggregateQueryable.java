//
//Copyright (c) 1998,2007 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data.spi;


import java.util.ArrayList;
import java.util.LinkedList;

import spiralcraft.data.Identifier;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;

import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.kit.ConstantChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.ArrayUtil;

/**
 * <p>Adapts the Queryable interface to an Aggregate, to provide the
 *   functionality of Querying a single Type. A wrapper or subclass will
 *   supply the actual data in the form of the Aggregate interface.
 * </p>
 * 
 * <p>Queries against this Queryable will normally do a full scan of the
 *   data, unless the extending class provides optimizations.
 * </p>
 *   
 */
public abstract class AbstractAggregateQueryable<T extends Tuple>
  implements Queryable<T>
{ 
  
  protected final ClassLog log
    =ClassLog.getInstance(getClass());

  protected Level logLevel
    =ClassLog.getInitialDebugLevel(getClass(),null);
  
  protected abstract Aggregate<T> getAggregate()
    throws DataException;
  
  protected abstract Type<?> getResultType();
  
  protected Focus<Queryable<T>> selfFocus;
  {
    try
    { 
      selfFocus
        =new SimpleFocus<Queryable<T>>
          (ConstantChannel.<Queryable<T>>forBean(this));
    }
    catch (BindException x)
    { throw new RuntimeDataException("",x);
    }
    

  }
  
  @Override
  public boolean containsType(Type<?> type)
  { return type.isAssignableFrom(getResultType());
  }

  @Override
  public BoundQuery<?,T> getAll(Type<?> type) throws DataException
  {
    BoundScan scan=new BoundScan(new Scan(getResultType()),selfFocus);
    scan.resolve();
    return scan;
  }

  @Override
  public Type<?>[] getTypes()
  { return new Type<?>[] {getResultType()};
  }
  
  
  @Override
  public BoundQuery<?,T> solve(Query q, Focus<?> context)
    throws DataException
  { 
    if (q==null)
    { throw new IllegalArgumentException("Query cannot be null");
    }
    
    BoundQuery<?,T> ret=null;
    if (q instanceof Scan
        && q.getType().isAssignableFrom(getResultType())
        )
    { ret=new BoundScan((Scan) q,context);
    }
    else if ( (q instanceof EquiJoin)
        && (q.getSources().get(0) instanceof Scan)
        && q.getType().isAssignableFrom(getResultType())
        )
    { ret=new BoundIndexScan((EquiJoin) q,context);
    }
    return ret;
  }

  @Override
  public BoundQuery<?,T> query(Query q, Focus<?> context)
    throws DataException
  { 
    if (q==null)
    { throw new IllegalArgumentException("Query cannot be null");
    }
    
    BoundQuery<?,T> ret=solve(q,context);
    if (ret==null)
    { ret=q.solve(context, this);
    }
    ret.resolve();
    if (logLevel.isDebug())
    { log.debug(q.toString()+" bound to "+ret);
    }
    return ret;
  }
  
  class BoundScan
    extends BoundQuery<Scan,T>
  {
    
    private final boolean debugTrace;
    
    public BoundScan(Scan query,Focus<?> paramFocus)
    { 
      super(query,paramFocus);
      debugTrace=debugLevel.canLog(Level.TRACE);
    }
    
    @Override
    public SerialCursor<T> doExecute() throws DataException
    { 
      Aggregate<T> aggregate=getAggregate();
      if (aggregate==null)
      {
        throw new DataException
          ("Aggregate of "+getType().getURI()+" from "
            +AbstractAggregateQueryable.this.getClass().getName()
            +" is null- cannot perform query"
          );
      }
      // return new BoundScanScrollableCursor(aggregate); 
      if (debugTrace)
      { log.trace(toString()+": Executing BoundScan of "+getType().getURI());
      }
      return new ListCursor<T>(aggregate);
    } 
    
    class BoundScanScrollableCursor
      extends BoundQueryScrollableCursor
    {
      private final ScrollableCursor<T> cursor;
     
      @Override
      public Identifier getRelationId()
      { return null;
      }
      
      public BoundScanScrollableCursor(Aggregate<T> aggregate)
      { cursor=new ListCursor<T>(aggregate);
      }
      
      @Override
      public boolean next()
        throws DataException
      {
        if (cursor.next())
        { 
          dataAvailable(cursor.getTuple());
          return true;
        }
        else
        { return false;
        }
      }

      @Override
      public void moveAfterLast()
        throws DataException
      { cursor.moveAfterLast();
      }

      @Override
      public void moveBeforeFirst()
        throws DataException
      { cursor.moveBeforeFirst();
      }

      @Override
      public boolean moveFirst()
        throws DataException
      { 
        if (cursor.moveFirst())
        { 
          dataAvailable(cursor.getTuple());
          return true;
        }
        else
        { return false;
        }
      }

      @Override
      public boolean moveLast()
        throws DataException
      { 
        if (cursor.moveLast())
        { 
          dataAvailable(cursor.getTuple());
          return true;
        }
        else
        { return false;
        }
      }

      @Override
      public boolean previous()
        throws DataException
      { 
        if (cursor.previous())
        { 
          dataAvailable(cursor.getTuple());
          return true;
        }
        else
        { return false;
        }
        
      }
      
      @Override
      public void close()
        throws DataException
      { cursor.close();
      }
    }
  }
  

  public class BoundIndexScan
    extends BoundQuery<EquiJoin,T>
  {
    private final Projection<T> projection;
    private final Channel<?>[] parameters;
    private final boolean debugTrace;
    
    @SuppressWarnings("unchecked") // Projection<Tuple> to Projection<T>
    public BoundIndexScan(EquiJoin ej,Focus<?> context)
      throws DataException
    { 
      super(ej,context);
      // Create a focus to resolve all the RHSExpressions
      Focus<?> focus=new TeleFocus<Void>(context,null);
      
      ArrayList<Expression<?>> lhsExpressions=ej.getLHSExpressions();

      // TODO: Check keys here: This should really be 
      //   getResultType().getProjection
      //       (lhsExpressions.toArray(new Expression[0]))
      
      if (getResultType().getScheme()==null)
      { throw new DataException("Type has no scheme: "+getResultType());
      }
      projection
        =(Projection<T>) getResultType().getScheme().getProjection
          (lhsExpressions.toArray(new Expression<?>[0]));

      ArrayList<Expression<?>> rhsExpressions=ej.getRHSExpressions();
      parameters=new Channel<?>[rhsExpressions.size()];
      int i=0;
      for (Expression<?> expr : rhsExpressions)
      { 
        try
        { parameters[i]=focus.bind(expr);
        }
        catch (BindException x)
        { 
          throw new DataException
            ("Error binding EquiJoin parameter expression "+expr,x);
        }
        
        Reflector<?> paramReflector=parameters[i].getReflector();
        Reflector<?> fieldReflector
          =projection.getFieldByIndex(i).getContentReflector();
        if (paramReflector!=fieldReflector
            && paramReflector!=null
            && fieldReflector!=null
            && !paramReflector.isAssignableFrom(fieldReflector)
            && !fieldReflector.isAssignableFrom(paramReflector)
            )
        { 
          throw new DataException
            ("Types are not comparable: "
              +expr.toString()+"("+paramReflector.getTypeURI()+")"
              +" cannot be compared to "
              +projection.getFieldByIndex(i).getURI()
              +"("+fieldReflector.getTypeURI()+")"
            );
        }
        i++;
      }
      
      debugTrace=debugLevel.canLog(Level.TRACE);
    }
    
    
    @Override
    public String toString()
    { return super.toString()+" projection="+projection.toString();
    }
    
    @Override
    public SerialCursor<T> doExecute() throws DataException
    { 
      KeyedListAggregate<T> aggregate=(KeyedListAggregate<T>) getAggregate();
      if (aggregate==null)
      { throw new DataException("Aggregate is null- cannot perform query");
      }
      Aggregate.Index<T> index=aggregate.getIndex(projection, true);
      
      Object[] parameterData=new Object[parameters.length];
      for (int i=0;i<parameters.length;i++)
      { parameterData[i]=parameters[i].get();
      }
      KeyTuple key=new KeyTuple(projection,parameterData,true);
      Aggregate<T> result=index.get(key);
      
      if (debugTrace)
      { 
        log.trace
          (toString()+": Executing BoundIndexScan of "+getType().getURI()+"#"
            +projection.toString()
            +" with ["+ArrayUtil.format(parameterData,",","")+"]"
          );
      }      
      if (result==null)
      { 
        return new ListCursor<T>
          (getResultType().getScheme(),new LinkedList<T>());
      }
      else
      { return new ListCursor<T>(result);
      }
    } 
  }
}
