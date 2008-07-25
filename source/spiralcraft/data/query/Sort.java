//
// Copyright (c) 1998,2007 Michael Toth
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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.log.ClassLogger;
import spiralcraft.util.ArrayUtil;

import spiralcraft.data.Order;
import spiralcraft.data.OrderElement;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.spi.ListCursor;
import spiralcraft.data.spi.OrderComparator;

/**
 * A Query which sorts the results of a a source Query
 */
public class Sort
  extends Query
{
  private static final ClassLogger log
    =ClassLogger.getInstance(Selection.class);
  
  private Order order;
  private String[] names;
  
  public Sort()
  { 
  }
  
  public Sort
      (Distinct baseQuery
      ,Order order
      )
  { 
    super(baseQuery);
    this.order=order;
  }
  
  /**
   * Construct a Selection which reads data from the specified source Query and filters
   *   data according to the specified constraints expression.
   */
  public Sort(Query source,Order order)
  { 
    this.order=order;
    addSource(source);
  }

  public void setOrderElements(OrderElement<?>[] elements)
  { 
    order=new Order();
    order.setElements(elements);
  }
  
  
  /**
   *@return the Order which defines this Sort query
   */
  public Order getOrder()
  { return order;
  }
  
  public void setOrder(Order order)
  { this.order=order;
  }
  
  public void resolve()
    throws DataException
  { 
    super.resolve();
    List<Query> sources=getSources();
    if (sources==null || sources.size()!=1)
    { throw new DataException
        ("Sort must have a single source source, not "
        +(sources!=null?sources.size():"0")
        );
    }
    
    if (debug)
    { log.fine("Creating projection for "+ArrayUtil.format(names,",",""));
    }
    this.type=sources.get(0).getType();
    
    
  }
  
  public FieldSet getFieldSet()
  { 
    if (sources!=null)
    { return sources.get(0).getFieldSet();
    }
    return null;
  }
 
  public void setSource(Query source)
  { 
    addSource(source);
  }
  
  
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { return new SortBinding<Sort,T>(this,focus,store);
   
  }
  
  public String toString()
  { return super.toString()
      +"[order="+order+"]: sources="
      +getSources().toString();
  }
    
}

class SortBinding<Tq extends Sort,T extends Tuple>
  extends BoundQuery<Tq,T>
{
  private OrderComparator comparator;
  private Focus<?> paramFocus;
  private boolean resolved;
  private BoundQuery<?,T> source;
  
  @SuppressWarnings("unchecked")
  public SortBinding
    (Tq query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    setQuery(query);
    this.paramFocus=paramFocus;
    
    source=(BoundQuery<?,T>) store.query(getQuery().getSources().get(0),paramFocus);

    if (source==null)
    { 
      throw new DataException
        ("Querying "+store+" returned null (unsupported Type?) for "
          +getQuery().getSources().get(0));
    }
    
  }

  public void resolve() throws DataException
  { 
    if (!resolved)
    {
      super.resolve();
      try
      {
        comparator
          =new OrderComparator
            (getQuery().getOrder()
            ,getQuery().getFieldSet()
            ,paramFocus
            );
      }
      catch (BindException x)
      { 
        throw new DataException
          ("Error creating Comparator for sort: "+toString(),x);
      }
      resolved=true;
    }
  }
  
  public SerialCursor<T> execute()
    throws DataException
  {
    return new SortScrollableCursor(source.execute());
  }
  
  protected SerialCursor<T> newSerialCursor(SerialCursor<T> source)
    throws DataException
  { return new SortScrollableCursor(source);
  }
  
  protected ScrollableCursor<T> newScrollableCursor(SerialCursor<T> source)
    throws DataException
  { return new SortScrollableCursor(source);
  }

  class SortScrollableCursor
    extends ListCursor<T>
  {
    
    { data=new LinkedList<T>();
    }
    
    
      
    @SuppressWarnings("unchecked")
    public SortScrollableCursor(SerialCursor<T> source)
      throws DataException
    { 
      super(source.dataGetFieldSet());
      while (source.dataNext())
      { data.add(source.dataGetTuple());
      }
      Collections.sort(data,(Comparator<T>) comparator);
    }
  
    
    public Type<?> getResultType()
    { return getQuery().getType();
    }

  }

}