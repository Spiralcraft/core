//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.lang.kit;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.MapDecorator;
import spiralcraft.lang.spi.SourcedChannel;

public class MapLookupChannel<T,K,V>
  extends SourcedChannel<T,V>
{
  
  private final MapDecorator<T,K,V> decorator;
  private final Channel<K> subscriptChannel;
  
  public MapLookupChannel
    (MapDecorator<T,K,V> decorator
    ,Channel<K> subscriptChannel
    )
  { 
    super(decorator.getValueReflector(),decorator.getSource());
    this.decorator=decorator;
    this.subscriptChannel=subscriptChannel;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected V retrieve()
  { return decorator.get(subscriptChannel.get());
  }

  @Override
  protected boolean store(V val)
    throws AccessException
  { return decorator.put(subscriptChannel.get(),val);
  }
  
  @Override
  public boolean isWritable()
  { return true;
  }

}
