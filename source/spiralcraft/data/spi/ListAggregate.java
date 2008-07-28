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
package spiralcraft.data.spi;

import spiralcraft.data.Aggregate;
import spiralcraft.data.Projection;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds a aggregation of objects of a common type.
 */
public class ListAggregate<T>
  extends AbstractAggregate<T>
{
  protected final List<T> list;
    
  /**
   * <p>Create a new ListAggregate backed by the specified List
   * </p>
   * 
   * @param type
   */
  public ListAggregate(Type<?> type,List<T> impl)
  { 
    super(type);
    list=impl;
    
  }

  /**
   * <p>Create a new ListAggregate backed by an ArrayList
   * </p>
   * 
   * @param type
   */
  public ListAggregate(Type<?> type)
  { 
    super(type);
    list=new ArrayList<T>();
  }

  /**
   * <p>Performs a shallow copy of contents of the original list
   * 
   * @param original
   */
  public ListAggregate(Aggregate<T> original)
  { 
    super(original.getType());
    list=new ArrayList<T>(original.size());
    for (T element:original)
    { list.add(element);
    }
  }
  
  /**
   * <p>Performs a shallow copy of contents of the original list
   * 
   * @param original
   */
  public ListAggregate(Aggregate<T> original,List<T> impl)
  { 
    super(original.getType());
    list=impl;
    for (T element:original)
    { list.add(element);
    }
  }
    
  public Iterator<T> iterator()
  { 
    // XXX Block remove() if not mutable- create a ReadOnlyIterator wrapper
    return list.iterator();
  }
  
  public int size()
  { return list.size();
  }
    

  public T get(int index)
  { return list.get(index);
  }

  @SuppressWarnings("unchecked")
  public Aggregate<T> snapshot() throws DataException
  { 
    if (isMutable())
    { 
      if (list instanceof ArrayList)
      { return new ListAggregate<T>(this,new ArrayList(list.size()));
      }
      else
      {
      
        try
        {
          return new ListAggregate<T>
            (this,list.getClass().newInstance());
        }
        catch (InstantiationException x)
        { throw new DataException("Error creating backing list",x);
        }
        catch (IllegalAccessException x)
        { throw new DataException("Error creating backing list",x);
        }
      }
    }
    else
    {
      // XXX Should we return snapshots of all the contained data?
      return this;
    }
  }
  
  public Index<T> getIndex(Projection projection,boolean create)
    throws DataException
  { return null;
  }

}