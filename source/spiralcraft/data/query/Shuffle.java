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
import java.util.Random;

import spiralcraft.lang.Focus;
//import spiralcraft.log.ClassLogger;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.spi.ListCursor;

/**
 * <p>A Query which returns the results of a a source Query in random order
 * </p>
 * 
 * <p>The random order is determined by XORing the Tuple's hash code by
 *   a random value determined at the beginning of each shuffle.
 * </p>
 */
public class Shuffle
  extends Query
{
//  private static final ClassLogger log
//    =ClassLogger.getInstance(Selection.class);
  
  
  public Shuffle()
  { 
  }
 
  public Shuffle(Query source)
  { addSource(source);
  }

  @Override
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
    
    this.type=sources.get(0).getType();
  }
  
  @Override
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
  
  
  @Override
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { return new ShuffleBinding<Shuffle,T>(this,focus,store);
  }
  
  @Override
  public String toString()
  { return super.toString();
  }
    
}

class ShuffleBinding<Tq extends Shuffle,T extends Tuple>
  extends BoundQuery<Tq,T>
{
  private boolean resolved;
  private BoundQuery<?,T> source;
  private Random random=new Random();
  
  @SuppressWarnings("unchecked")
  public ShuffleBinding
    (Tq query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    setQuery(query);
    
    source=(BoundQuery<?,T>) store.query(getQuery().getSources().get(0),paramFocus);

    if (source==null)
    { 
      throw new DataException
        ("Querying "+store+" returned null (unsupported Type?) for "
          +getQuery().getSources().get(0));
    }
    
  }

  @Override
  public void resolve() throws DataException
  { 
    if (!resolved)
    {
      super.resolve();
      resolved=true;
    }
  }
  
  @Override
  public SerialCursor<T> execute()
    throws DataException
  {
    SerialCursor<T> sourceCursor=source.execute();
    try
    { return new ShuffleScrollableCursor(sourceCursor);
    }
    finally
    { sourceCursor.close();
    }
    
  }

  class ShuffleComparator
    implements Comparator<Tuple>
  {
    private int val=random.nextInt();

    @Override
    public int compare(
      Tuple o1,
      Tuple o2)
    {
      Integer h1=o1==null?val:o1.hashCode() ^ val;
      Integer h2=o2==null?val:o2.hashCode() ^ val;
      return h1.compareTo(h2);
    }

  }
  
  protected class ShuffleScrollableCursor
    extends ListCursor<T>
  {
    
    { data=new LinkedList<T>();
    }
    
    @SuppressWarnings("unchecked")
    public ShuffleScrollableCursor(SerialCursor<T> source)
      throws DataException
    { 
      super(source.getFieldSet());
      while (source.next())
      { 
        T tuple=source.getTuple();
        if (tuple.isVolatile())
        { tuple=(T) tuple.snapshot();
        }
        data.add(tuple);        
      }
      Collections.sort(data,new ShuffleComparator());
    }
    
    @Override
    public Type<?> getResultType()
    { return getQuery().getType();
    }

  }

}