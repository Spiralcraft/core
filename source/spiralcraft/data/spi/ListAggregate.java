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

import spiralcraft.command.Command;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Projection;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.lang.Channel;
import spiralcraft.log.ClassLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds a aggregation of objects of a common type.
 */
public class ListAggregate<T>
  extends AbstractAggregate<T>
{
  private static final ClassLog log=ClassLog.getInstance(ListAggregate.class);
  
  protected final List<T> list;
  protected int expectedSize;
  protected Channel<Command<?,?,?>> fillCommand;
  private boolean debug=false;
  private boolean fetchable;
    
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
    
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  @Override
  public boolean contains(T value)
  { 
    if (fillCommand!=null)
    { fillRest();
    }
    return list.contains(value);
  }
  /**
   * <p>A command to run when the backing list runs out of entries
   * </p>
   */
  public void setFillCommand(Channel<Command<?,?,?>> fillCommand)
  { 
    this.fillCommand=fillCommand;
    this.fetchable=true;
  }
  
  @Override
  public Iterator<T> iterator()
  { 
    if (!fetchable)
    {
      // XXX Block remove() if not mutable- create a ReadOnlyIterator wrapper
      return list.iterator();
    }
    else
    { return new RefillIterator();
    }
  }
  
  @Override
  public int size()
  { 
    if (fillCommand!=null)
    { 
      if (expectedSize>0 || !fetchable)
      { return Math.max(list.size(),expectedSize);
      }
      else
      { 
        // Force full fetch
        fillRest();
        return list.size();
      }
    }
    else
    { return list.size();
    }
  }
  
  @Override
  public boolean isEmpty()
  { 
    if (list.isEmpty())
    { return true;
    }
    else if (fillCommand!=null)
    {
      if (expectedSize>0 || !fetchable)
      { return expectedSize<=0;
      }
      else
      { 
        fill();
        return list.isEmpty();
      }
    }
    else
    { return true;
    }
  }
    

  protected void fillRest()
  {
    while (fetchable && (expectedSize==0 || list.size()<expectedSize))
    { fill();
    }
  }
  
  public void setExpectedSize(int expectedSize)
  { this.expectedSize=expectedSize;
  }
  
  @Override
  public T get(int index)
  { 
    if (fillCommand!=null)
    { fillTo(index);
    }
    if (index<list.size())
    { return list.get(index);
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Aggregate<T> snapshot() throws DataException
  { 
    if (fillCommand!=null)
    { fillRest();
    }
    
    if (isMutable())
    { 
      if (list instanceof ArrayList)
      { return new ListAggregate<T>(this,new ArrayList<T>(list.size()));
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
  
  @Override
  public Index<T> getIndex(Projection<T> projection,boolean create)
    throws DataException
  { return null;
  }

  protected void fillTo(int index)
  {
    while (fetchable
           && index>=list.size() 
           && (expectedSize==0 || index<expectedSize
              )
          )
    { fill(); 
    }
  }
  
  /**
   * <p>Invokes the fillCommand command to populate the list
   * </p>
   *  
   * @return
   */
  protected int fill()
  {
    
    int size=list.size();
    if (debug)
    { log.fine("Filling list: expectedSize="+expectedSize+"  size="+size);
    }
    Command<?,?,?> command=fillCommand.get();
    command.execute();
    if (command.getException()!=null)
    { 
      // Abort further fetching (make optional?)
      expectedSize=list.size();
      fetchable=false;
   
      throw new RuntimeDataException
        ("Error filling lazy ListAggregate"
        ,command.getException()
        );
    }
    if (list.size()==size)
    { 
      if (debug)
      { log.fine("No more data added.");
      }      
      expectedSize=size;
      fetchable=false;
      return 0;
    }
    else
    { 
      if (debug)
      { log.fine("Added "+(list.size()-size)+" items");
      }      
      return list.size()-size;
    }
  }
  
  class RefillIterator
    implements Iterator<T>
  {
    private int pos=0;
    
    public RefillIterator()
    { 
    }

    @Override
    public boolean hasNext()
    { 
      if (pos>=list.size())
      { 
        if ( fetchable && (expectedSize==0 || pos<expectedSize) && fill()>0)
        { return true;
        }
        return false;
        
      }
      return true;
    }

    @Override
    public T next()
    {

      try
      { return list.get(pos);
      }
      finally
      { pos++;
      }
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException("remove() is not supported");
      
    }
  }

}