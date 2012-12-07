//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.lang.spi;

import java.util.concurrent.atomic.AtomicLong;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.time.Clock;

/**
 * Provides a stable reference to a snapshot of a source value. The snapshot
 *   will be updated when it is explicitly marked stale or when 
 *   "refresh" is called.
 * 
 * @author mike
 *
 * @param <T>
 */
public class CachedChannel<T>
  extends SourcedChannel<T,T>
{

  private T value;
  private final AtomicLong lastRefresh
    =new AtomicLong(0);
  
  
  public CachedChannel(Channel<T> source)
  { super(source.getReflector(), source);
  }

  protected boolean isStale()
  {
    
    if (lastRefresh.get()==0)
    { return true;
    }
    return false;
  }
  
  public void refresh()
  { 
    value=source.get();
    lastRefresh.set(Clock.instance().approxTimeMillis());
  }
  
  @Override
  protected T retrieve()
  { 
    if (isStale())
    { refresh();
    }
    return value;
  }

  @Override
  protected boolean store(
    Object val)
    throws AccessException
  { return false;
  }

}
