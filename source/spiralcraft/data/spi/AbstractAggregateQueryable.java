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
import spiralcraft.lang.TeleFocus;

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
  protected abstract Aggregate<T> getAggregate()
    throws DataException;
  
  protected abstract Type<?> getResultType();
  
  public boolean containsType(Type<?> type)
  { return type.isAssignableFrom(getResultType());
  }

  public BoundQuery<?,T> getAll(Type<?> type) throws DataException
  {
    BoundScan scan=new BoundScan(new Scan(getResultType()));
    scan.resolve();
    return scan;
  }

  public Type<?>[] getTypes()
  { return new Type[] {getResultType()};
  }

  public BoundQuery<?,T> query(Query q, Focus<?> context)
    throws DataException
  { 
    
    BoundQuery<?,T> ret;

    // Just do a scan, because we're just a dumb list
    ret=q.solve(context, this);
    ret.resolve();
    return ret;
  }
  
  class BoundScan
    extends BoundQuery<Scan,T>
  {
    
    public BoundScan(Scan query)
    { setQuery(query);
    }
    
    @Override
    public SerialCursor<T> execute() throws DataException
    { 
      Aggregate<T> aggregate=getAggregate();
      if (aggregate==null)
      { throw new DataException("Aggregate is null- cannot perform query");
      }
      // return new BoundScanScrollableCursor(aggregate); 
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
      
      public boolean dataNext()
        throws DataException
      {
        if (cursor.dataNext())
        { 
          dataAvailable(cursor.dataGetTuple());
          return true;
        }
        else
        { return false;
        }
      }

      @Override
      public void dataMoveAfterLast()
        throws DataException
      { cursor.dataMoveAfterLast();
      }

      @Override
      public void dataMoveBeforeFirst()
        throws DataException
      { cursor.dataMoveBeforeFirst();
      }

      @Override
      public boolean dataMoveFirst()
        throws DataException
      { 
        if (cursor.dataMoveFirst())
        { 
          dataAvailable(cursor.dataGetTuple());
          return true;
        }
        else
        { return false;
        }
      }

      @Override
      public boolean dataMoveLast()
        throws DataException
      { 
        if (cursor.dataMoveLast())
        { 
          dataAvailable(cursor.dataGetTuple());
          return true;
        }
        else
        { return false;
        }
      }

      @Override
      public boolean dataPrevious()
        throws DataException
      { 
        if (cursor.dataPrevious())
        { 
          dataAvailable(cursor.dataGetTuple());
          return true;
        }
        else
        { return false;
        }
        
      }
    }
  }
  

  public class BoundIndexScan
    extends BoundQuery<EquiJoin,T>
  {
    private final Projection projection;
    private final Channel<?>[] parameters;
    
    public BoundIndexScan(EquiJoin ej,Focus<?> context)
      throws DataException
    { 
      // Create a focus to resolve all the LHSExpressions
      Focus<?> focus=new TeleFocus<Void>(context,null);
      
      ArrayList<Expression<?>> lhsExpressions=ej.getLHSExpressions();

      projection
        =getResultType().getScheme().getProjection
          (lhsExpressions.toArray(new Expression[0]));

      ArrayList<Expression<?>> rhsExpressions=ej.getRHSExpressions();
      parameters=new Channel<?>[rhsExpressions.size()];
      int i=0;
      for (Expression<?> expr : rhsExpressions)
      { 
        try
        { parameters[i++]=focus.bind(expr);
        }
        catch (BindException x)
        { 
          throw new DataException
            ("Error binding EquiJoin parameter expression "+expr,x);
        }
      }
      
      setQuery(ej);
    }
    
    
    @Override
    public SerialCursor<T> execute() throws DataException
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
