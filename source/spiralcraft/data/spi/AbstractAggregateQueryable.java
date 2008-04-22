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

import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;

import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;


import spiralcraft.lang.Focus;

/**
 * <p>Adapts the Queryable interface to an Aggregate, to provide the
 *   functionality of Querying a single Type. A wrapper or subclass will
 *   supply the actual data in the form of the Aggregate interface.
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
    BoundQuery<?,T> ret=q.getDefaultBinding(context, this);
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
      return new BoundScanScrollableCursor(aggregate); 
    } 
    
    class BoundScanScrollableCursor
      extends BoundQueryScrollableCursor
    {
      private final ScrollableCursor<T> cursor;
     
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
  

  
}
