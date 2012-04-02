//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

import spiralcraft.common.Immutable;

/**
 * An immutable sequence of items
 *
 * @param <T>
 */
@Immutable
public class Sequence<T>
  implements Iterable<T>
{

  private final int len;
  private final int start;
  private final T[] data;

  public Sequence(T[] data)
  { 
    if (data==null)
    { 
      throw new IllegalArgumentException
        ("Cannot construct a sequence with null data");
    }
    this.data=data;
    this.start=0;
    this.len=data.length;
  }
  
  public Sequence(T[] data,int start,int len)
  {
    if (data==null)
    { 
      throw new IllegalArgumentException
        ("Cannot construct a sequence with null data");
    }
    this.data=data;
    this.start=0;
    this.len=data.length;
  }
  
  public int size()
  { return len;
  }
  
  public T get(Integer index)
  { return data[index];
  }
  
  public T getFirst()
  { return data[start];
  }
  
  public T getLast()
  { return data[start+len-1];
  }
  
  public Sequence<T> append(T item)
  { 
    @SuppressWarnings("unchecked")
    T[] array=(T[]) Array.newInstance(data.getClass().getComponentType(),1);
    return concat(array);
  }
  
  @SuppressWarnings("unchecked")
  public Sequence<T> concat(T[] items)
  { 
    T[] array=(T[]) Array.newInstance
       (data.getClass().getComponentType(),len+items.length);
    System.arraycopy(data,start,array,0,len);
    System.arraycopy(items,0,array,len,items.length);
    
    return new Sequence<T>(array);
  }

  public Sequence<T> removeFirst()
  { return new Sequence<T>(data,start+1,len);
  }

  public boolean isEmpty()
  { return len<=0;
  }
  
  @Override
  public Iterator<T> iterator()
  { return ArrayUtil.iterator(data,start,len);
  }
  
  public T[] toArray()
  {
    if (start==0 && len==data.length)
    { return data;
    }
    else
    { 
      @SuppressWarnings("unchecked")
      T[] ret=(T[]) Array.newInstance(data.getClass().getComponentType(),len);
      System.arraycopy(data,start,ret,0,len);
      return ret;
    }
  }
  
  public <Tl extends List<T>> Tl toList(Tl list)
  {
    for (T item: this)
    { list.add(item);
    }
    return list;
  }
  
  public String format(String separator)
  {
    StringBuffer buf=new StringBuffer();
    boolean first=true;
    for (T item: this)
    { 
      if (!first)
      { buf.append(separator);
      }
      else
      { first=false;
      }
      if (item!=null)
      { buf.append(item.toString());
      }
    }
    return buf.toString();
  }
  
  @Override
  public String toString()
  { return "{"+format(",")+"}";
  }
  
}
