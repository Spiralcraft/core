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
package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;

/**
 * <p>Binds a set of BindingChannels to a ThreadLocal target location and
 *   updates all objects retrieved from the channel.
 * </p>
 *   
 * 
 * @author mike
 *
 * @param <T>
 */
public class GatherChannel<T>
  extends AbstractChannel<T>
{

  private final Channel<T> source;
  private final ThreadLocalChannel<T> localChannel;
  private final BindingChannel<?>[] bindings;
  
  public GatherChannel
    (Channel<T> source
    ,BindingChannel<?>[] bindings
    )
    throws BindException
  { 
    super(source.getReflector());
    this.source=source;
    localChannel=new ThreadLocalChannel<T>(source.getReflector());
    this.bindings=bindings;
    
    Focus<?> focus=new SimpleFocus<T>(localChannel);
    for (BindingChannel<?> binding:bindings)
    { binding.bindTarget(focus);
    }
    
  }
  
  @Override
  protected T retrieve()
  {
    T val=source.get();
    
    if (val!=null)
    {
      localChannel.push(val);
      try
      {
        for (BindingChannel<?> channel : bindings)
        { channel.get();
        }
        // Allow bindings to replace the value
        val=localChannel.get();
      }
      finally
      { localChannel.pop();
      }
    }
      
    return val;
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { return source.set(val);
  }
  
  @Override
  public boolean isWritable()
  { return source.isWritable();
  }

}
