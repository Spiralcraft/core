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

import spiralcraft.data.DataComposite;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Identifier;
import spiralcraft.data.Key;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.lang.BindException;
import spiralcraft.util.KeyedList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Holds a aggregation of objects of a common type.
 */
public class KeyedListAggregate<T>
  implements Aggregate<T>
{
  protected final KeyedList<T> list;
  
  private final Type<?> type;
  private Identifier id;
  private ArrayList<Index<T>> indices;
  private Map<Projection,Index<T>> indexMap;
  
  /**
   * <p>Create a new ListAggregate backed by the specified List
   * </p>
   * 
   * @param type
   */
  public KeyedListAggregate(Type<?> type,KeyedList<T> impl)
  { 
    this.type=type;
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
    this.type=type;
    list=new KeyedList<T>(new ArrayList<T>());
  }

  /**
   * <p>Performs a shallow copy of contents of the original list
   * 
   * @param original
   */
  public KeyedListAggregate(Aggregate<T> original)
  { 
    list=new KeyedList<T>(new ArrayList<T>(original.size()));
    type=original.getType();
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
    list=impl;
    type=original.getType();
    for (T element:original)
    { list.add(element);
    }
  }
  
  
  public Identifier getId()
  { return id;
  }
  
  public void setId(Identifier id)
  { this.id=id;
  }
  
  public boolean isAggregate()
  { return true;
  }
  
  public Aggregate<?> asAggregate()
  { return this;
  }
  
  public boolean isTuple()
  { return false;
  }

  public Tuple asTuple()
  { throw new UnsupportedOperationException("An aggregate is not a Tuple");
  }
  
  
  public Type<?> getType()
  { return type;
  }
  
  public Iterator<T> iterator()
  { 
    // XXX Block remove() if not mutable- create a ReadOnlyIterator wrapper
    return list.iterator();
  }
  
  public int size()
  { return list.size();
  }
    
  public String toString()
  {
    StringBuilder builder=new StringBuilder();
    builder.append(getClass().getName()+"{");
    boolean first=true;
    for (Object o:list)
    {
      if (!first)
      { builder.append(",");
      }
      else
      { first=false;
      }
      builder.append(o.toString());
      
    }
    builder.append("}");
    return builder.toString();

  }
  
  public String toText(String indent)
    throws DataException
  {
    StringBuilder builder=new StringBuilder();
    builder.append("\r\n").append(indent);
    builder.append("{");
    boolean first=true;
    for (Object o:list)
    {
      if (!first)
      { 
        builder.append("\r\n").append(indent);
        builder.append(",");
      }
      else
      { first=false;
      }
      if (o instanceof DataComposite)
      { builder.append(((DataComposite) o).toText(indent+"  "));
      }
      else
      { builder.append(o.toString());
      }
      
    }
    builder.append("\r\n").append(indent);
    builder.append("}");
    return builder.toString();
  }

  public T get(int index)
  { return list.get(index);
  }

  public boolean isMutable()
  { return false;
  }

  @SuppressWarnings("unchecked")
  public Aggregate<T> snapshot() throws DataException
  { 
    if (isMutable())
    { 
      try
      {
        return new KeyedListAggregate<T>
          (this,list.getClass().newInstance());
      }
      catch (InstantiationException x)
      { throw new DataException("Error creating backing list",x);
      }
      catch (IllegalAccessException x)
      { throw new DataException("Error creating backing list",x);
      }
    }
    else
    {
      // XXX Should we return snapshots of all the contained data?
      return this;
    }
  }
  
  private synchronized void createIndexList()
  {
    if (indices==null)
    { indices=new ArrayList<Index<T>>();
    }
  }

  private synchronized void createIndexMap()
  {
    if (indexMap==null)
    { indexMap=new HashMap<Projection,Index<T>>();
    }
  }
  
  private synchronized Index<T> createKeyIndex(int keyIndex)
    throws DataException
  {
    while (indices.size()<=keyIndex)
    { indices.add(null);
    }
    if (indices.get(keyIndex)!=null)
    { return indices.get(keyIndex);
    }
    Key key=getType().getContentType().getScheme().getKeyByIndex(keyIndex);
    HashMap<KeyTuple,List<T>> map=new HashMap<KeyTuple,List<T>>();
    Index<T> index=new ListIndex(map,key);
    indices.set(keyIndex,index);
    return index;
  }
  
  private synchronized Index<T> createIndex(Projection projection)
    throws DataException
  {
    Index<T> index=indexMap.get(projection);
    if (index!=null)
    { return index;
    }

    HashMap<KeyTuple,List<T>> map=new HashMap<KeyTuple,List<T>>();
    index=new ListIndex(map,projection);
    indexMap.put(projection,index);
    return index;
  }

  public Index<T> getIndex(Projection projection,boolean create)
    throws DataException
  {
    if (list==null)
    { return null;
    }
    
    
    Index<T> index=null;
    if (projection instanceof Key)
    { 
      // Find it in the key list.
      
      if (indices==null && create)
      { createIndexList();
      }
    
      if (indices==null)
      { return null;
      }
      
      Key key=(Key) projection;
      if (indices.size()>key.getIndex())
      { index=indices.get(key.getIndex());
      }
      if (index==null && create)
      { index=createKeyIndex(key.getIndex());
      }
    }
    
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
    
    public ListIndex(Map<KeyTuple,List<T>> mapImpl,Projection projection)
      throws DataException
    { 
      try
      {
        backingIndex
          =list.<KeyTuple>addMap
            (mapImpl
            ,new DataKeyFunction<T>(projection)
            );
      }
      catch (BindException x)
      { throw new DataException("Error binding index function",x);
      }
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
    public T getOne(KeyTuple key)
    { return backingIndex.getOne(key);
    }
  }
}