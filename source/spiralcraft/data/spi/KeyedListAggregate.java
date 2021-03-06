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
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.KeyedList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Holds a aggregation of objects of a common type.
 */
public class KeyedListAggregate<T>
  extends AbstractAggregate<T>
{
  private static final ClassLog log
    =ClassLog.getInstance(KeyedListAggregate.class);
  
  private static final Level logLevel
    =ClassLog.getInitialDebugLevel(KeyedListAggregate.class,null);
  
  protected final KeyedList<T> list;
  
  private Map<Projection<T>,Index<T>> indexMap;
  
  /**
   * <p>Create a new ListAggregate backed by the specified List
   * </p>
   * 
   * @param type
   */
  public KeyedListAggregate(Type<?> type,KeyedList<T> impl)
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
  public KeyedListAggregate(Type<?> type)
  { 
    super(type);
    list=new KeyedList<T>(new ArrayList<T>());
  }

  /**
   * <p>Performs a shallow copy of contents of the original list
   * 
   * @param original
   */
  public KeyedListAggregate(Aggregate<T> original)
  { 
    super(original.getType());
    list=new KeyedList<T>(new ArrayList<T>(original.size()));
    for (T element:original)
    { list.add(element);
    }
  }
  
  /**
   * <p>Performs a shallow copy of contents of the original list
   * 
   * @param original
   */
  public KeyedListAggregate(Aggregate<T> original,KeyedList<T> impl)
  { 
    super(original.getType());
    list=impl;
    for (T element:original)
    { list.add(element);
    }
  }
  
  
  @Override
  public Iterator<T> iterator()
  { 
    // XXX Block remove() if not mutable- create a ReadOnlyIterator wrapper
    return list.iterator();
  }
  
  @Override
  public int size()
  { return list.size();
  }
    
  @Override
  public boolean isEmpty()
  { return list.isEmpty();
  }
  
  @Override
  public T get(int index)
  { return list.get(index);
  }

  @Override
  public boolean contains(T value)
  { return list.contains(value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Aggregate<T> snapshot() throws DataException
  { 
    if (isMutable())
    { 
      try
      {
        return new KeyedListAggregate<T>
          (this,list.getClass().getDeclaredConstructor().newInstance());
      }
      catch (InstantiationException x)
      { throw new DataException("Error creating backing list",x);
      }
      catch (IllegalAccessException x)
      { throw new DataException("Error creating backing list",x);
      }
      catch (InvocationTargetException x)
      { throw new DataException("Error creating backing list",x);
      }
      catch (NoSuchMethodException x)
      { throw new DataException("Error creating backing list",x);
      }
    }
    else
    {
      // XXX Should we return snapshots of all the contained data?
      return this;
    }
  }
  

  private synchronized void createIndexMap()
  {
    if (indexMap==null)
    { indexMap=new HashMap<Projection<T>,Index<T>>();
    }
  }
  
  
  private synchronized Index<T> createIndex(Projection<T> projection)
    throws DataException
  {
    Index<T> index=indexMap.get(projection);
    if (index!=null)
    { return index;
    }

    if (logLevel.canLog(Level.DEBUG))
    { log.debug("Creating index on "+projection+" for "+getType().getURI());
    }
    HashMap<KeyTuple,List<T>> map=new HashMap<KeyTuple,List<T>>();
    index=new ListIndex(map,projection);
    indexMap.put(projection,index);
    return index;
  }

  @Override
  public Index<T> getIndex(Projection<T> projection,boolean create)
    throws DataException
  {
    if (list==null)
    { return null;
    }
    
    
    Index<T> index=null;

    
    if (index==null)
    { 
      if (indexMap==null && create)
      { createIndexMap();
      }
      
      if (indexMap==null)
      { return null;
      }
      
      index=indexMap.get(projection);
      if (index==null && create)
      { index=createIndex(projection);
      }

      // Find the projection in the index map
      
    }
    
    return index;

  }
  
  class ListIndex
    implements Index<T>
  {
    
    private final KeyedList<T>.Index<KeyTuple,T> backingIndex;
    
    public ListIndex(Map<KeyTuple,List<T>> mapImpl,Projection<T> projection)
    { 
      backingIndex
        =list.<KeyTuple>addMap
          (mapImpl
          ,projection.getKeyFunction()
          );
    }

    @Override
    public Aggregate<T> get(
      KeyTuple key)
    {
      List<T> value=backingIndex.get(key);
      if (value!=null)
      { return new ListAggregate<T>(getType(),value);
      }
      return null;
    }

    @Override
    public T getFirst(KeyTuple key)
    { return backingIndex.getFirst(key);
    }
  }
}