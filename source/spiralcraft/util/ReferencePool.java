//
// Copyright (c) 2010 Michael Toth
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

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import spiralcraft.common.callable.Sink;

/**
 * <p>Pools immutable object instances to coalesce references to identical 
 *   objects into a single weak reference to reduce memory and increase 
 *   throughput.
 * </p>
 * 
 * <p>When all references to the object are released, the reference in
 *   this map will be garbage collected
 * </p>
 * 
 * @author mike
 *
 */
public class ReferencePool<T>
{
  
  private WeakHashMap<T,WeakReference<T>> map
    =new WeakHashMap<T,WeakReference<T>>();
  
  private Sink<T> matchSink;
  private Sink<T> addSink;
  
  public void setMatchSink(Sink<T> matchSink)
  { this.matchSink=matchSink;
  }
  
  public void setAddSink(Sink<T> addSink)
  { this.addSink=addSink;
  }

  public synchronized T get(T value)
  { 
    WeakReference<T> ref=map.get(value);
    if (ref!=null)
    { 
      T result=ref.get();
      if (result!=null)
      { 
        if (matchSink!=null)
        { matchSink.accept(result);
        }
        return result;
      }
    }
    
    map.put(value,new WeakReference<T>(value));
    if (addSink!=null)
    { addSink.accept(value);
    }
    return value;
  }  

}
