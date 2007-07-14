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

import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.UnrecognizedTypeException;
import spiralcraft.data.Type;

import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;


import spiralcraft.lang.Focus;

/**
 * <P>Adapts the Queryable interface to an Aggregate, to provide the
 *   functionality of Querying a single Type. A wrapper or subclass will
 *   supply the actual data in the form of the Aggregate interface.
 *   
 *   
 * 
 */
public class AggregateQueryable<T extends Tuple>
  implements Queryable<T>
{
  private Aggregate<T> aggregate;
 
  public Aggregate<T> getAggregate()
  { return aggregate;
  }
  
  /**
   * Reset the aggregate that backs this Queryable
   */
  public void setAggregate(Aggregate<T> aggregate)
  { 
    this.aggregate=aggregate;
    // System.err.println("AggregateQueryable: aggregate="+aggregate);
  }
  
  public boolean containsType(Type<?> type)
  { return getAggregate().getType().getContentType()==type;
  }

  public BoundQuery<?,T> getAll(Type<?> type) throws DataException
  {
    if (getAggregate().getType().getContentType()!=type)
    { 
      throw new UnrecognizedTypeException
        (type,"Can only query Type "+aggregate.getType().getContentType());
    }
    
    BoundScan scan=new BoundScan(new Scan(type));
    scan.resolve();
    return scan;
  }

  public Type<?>[] getTypes()
  { return new Type[] {getAggregate().getType().getContentType()};
  }

  public BoundQuery<?,T> query(Query q, Focus<?> context)
    throws DataException
  { 
    BoundQuery<?,T> ret=q.bind(context, this);
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
    { return new BoundScanSerialCursor(); 
    } 
    
    class BoundScanSerialCursor
      extends BoundQuerySerialCursor
    {
      private final SerialCursor<T> cursor=new ListCursor<T>(getAggregate());
      
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
    }
  }
  

  
}
